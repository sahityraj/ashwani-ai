
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

// Chat is temporarily disabled until secure user
// authentication is added. Never put a server secret
// directly inside an Android APK.
app.post("/api/chat", limiter, (req, res) => {
  res.status(503).json({
    error: "Secure authentication is not configured yet."
  });
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`Ashwani AI Backend running on port ${PORT}`);
});
