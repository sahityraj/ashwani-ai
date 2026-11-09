const express = require("express");
const helmet = require("helmet");
const rateLimit = require("express-rate-limit");

const app = express();

app.set("trust proxy", 1);
app.use(helmet());
app.use(express.json({ limit: "20kb" }));

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  limit: 20,
  standardHeaders: "draft-7",
  legacyHeaders: false
});

app.get("/", (req, res) => {
  res.json({
    app: "Ashwani AI Backend",
    status: "running"
  });
});

app.get("/health", (req, res) => {
  res.json({ status: "ok" });
});

app.post("/api/chat", limiter, async (req, res) => {
  try {
    const apiKey = process.env.GROQ_API_KEY;

    if (!apiKey) {
      return res.status(503).json({
        error: "AI service is not configured."
      });
    }

    const messages = req.body.messages;

    if (
      !Array.isArray(messages) ||
      messages.length === 0 ||
      messages.length > 20
    ) {
      return res.status(400).json({
        error: "Invalid messages."
      });
    }

    const safeMessages = [];

    for (const message of messages) {
      if (
        !message ||
        !["user", "assistant"].includes(message.role) ||
        typeof message.content !== "string" ||
        message.content.length > 4000
      ) {
        return res.status(400).json({
          error: "Invalid message format."
        });
      }

      safeMessages.push({
        role: message.role,
        content: message.content
      });
    }

    if (safeMessages[safeMessages.length - 1].role !== "user") {
      return res.status(400).json({
        error: "Last message must be from user."
      });
    }

    const response = await fetch(
      "https://api.groq.com/openai/v1/chat/completions",
      {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${apiKey}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          model: process.env.GROQ_MODEL ||
            "llama-3.3-70b-versatile",
          messages: [
            {
              role: "system",
              content:
                "You are Ashwani AI. Reply clearly and politely. " +
                "Use Hindi when the user speaks Hindi, " +
                "and English when the user speaks English."
            },
            ...safeMessages
          ],
          temperature: 0.7,
          max_completion_tokens: 700
        }),
        signal: AbortSignal.timeout(30000)
      }
    );

    if (!response.ok) {
      return res.status(502).json({
        error: "AI provider request failed."
      });
    }

    const data = await response.json();
    const reply = data.choices?.[0]?.message?.content;

    if (!reply) {
      return res.status(502).json({
        error: "Empty AI response."
      });
    }

    return res.json({ reply });

  } catch (error) {
    return res.status(500).json({
      error: "Something went wrong. Please try again."
    });
  }
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`Ashwani AI Backend running on port ${PORT}`);
});
