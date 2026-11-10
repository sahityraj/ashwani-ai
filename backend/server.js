const express = require("express");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");

const app = express();
const PORT = process.env.PORT || 3000;

app.use(helmet());
app.use(express.json({ limit: "12kb" }));

// Basic rate limiting
const chatLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 10,
  standardHeaders: "draft-7",
  legacyHeaders: false,
  message: {
    error: "Too many requests. Please try again later."
  }
});

app.get("/", (req, res) => {
  res.json({
    name: "Ashwani AI Backend",
    status: "running"
  });
});

app.get("/health", (req, res) => {
  res.json({
    status: "ok",
    aiConfigured: Boolean(
      process.env.AI_API_URL &&
      process.env.AI_API_KEY &&
      process.env.AI_MODEL
    )
  });
});

app.post("/api/chat", chatLimiter, async (req, res) => {
  try {
    const message = req.body?.message;

    if (
      typeof message !== "string" ||
      message.trim().length === 0
    ) {
      return res.status(400).json({
        error: "Please provide a message."
      });
    }

    if (message.length > 4000) {
      return res.status(400).json({
        error: "Message is too long."
      });
    }

    const apiUrl = process.env.AI_API_URL;
    const apiKey = process.env.AI_API_KEY;
    const model = process.env.AI_MODEL;

    if (!apiUrl || !apiKey || !model) {
      return res.status(503).json({
        error: "AI service is not configured."
      });
    }

    const parsedUrl = new URL(apiUrl);

    if (
      parsedUrl.protocol !== "https:" &&
      parsedUrl.hostname !== "localhost"
    ) {
      return res.status(400).json({
        error: "HTTPS is required."
      });
    }

    const controller = new AbortController();
    const timeout = setTimeout(
      () => controller.abort(),
      25000
    );

    let response;

    try {
      response = await fetch(apiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${apiKey}`
        },
        body: JSON.stringify({
          model,
          messages: [
            {
              role: "system",
              content: "You are Ashwani AI. Reply helpfully and clearly. Use Hindi when the user writes in Hindi."
            },
            {
              role: "user",
              content: message.trim()
            }
          ]
        }),
        signal: controller.signal
      });
    } finally {
      clearTimeout(timeout);
    }

    if (!response.ok) {
      return res.status(502).json({
        error: "AI provider request failed."
      });
    }

    const data = await response.json();
    const reply = data?.choices?.[0]?.message?.content;

    if (typeof reply !== "string" || !reply.trim()) {
      return res.status(502).json({
        error: "AI provider returned an invalid response."
      });
    }

    return res.json({
      reply: reply.trim()
    });

  } catch (error) {
    console.error("Chat error:", error.name || "UnknownError");

    return res.status(500).json({
      error: "Something went wrong. Please try again."
    });
  }
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Ashwani AI backend running on port ${PORT}`);
});
