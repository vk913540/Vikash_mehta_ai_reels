import RunwayML from "@runwayml/sdk";
import express from "express";
import cors from "cors";

const app = express();
const PORT = process.env.PORT || 8080;
const runway = new RunwayML();

app.use(cors());
app.use(express.json());
app.use(express.static("public"));

const users = new Map();

function getUser(id) {
  if (!users.has(id)) {
    users.set(id, { freeReelUsed: false, premium: false });
  }
  return users.get(id);
}

app.get("/health", (req, res) => {
  res.json({ ok: true, app: "VIKASH MEHTA AI REELS" });
});

app.get("/v1/me/:userId", (req, res) => {
  res.json(getUser(req.params.userId));
});

app.post("/v1/subscribe", (req, res) => {
  const { userId } = req.body || {};

  if (!userId) {
    return res.status(400).json({
      error: "userId is required"
    });
  }

  const user = getUser(userId);
  user.premium = true;

  res.json({
    success: true,
    message: "Subscription activated",
    premium: true
  });
});


app.post("/v1/reels/generate", async (req, res) => {
  const { userId, prompt, duration } = req.body || {};

  if (!userId || !prompt) {
    return res.status(400).json({
      error: "userId and prompt are required"
    });
  }

  const user = getUser(userId);

  if (!user.premium && user.freeReelUsed) {
    return res.status(402).json({
      error: "FREE REEL USED",
      message: "Subscribe to create more reels"
    });
  }

  if (!user.premium) {
    user.freeReelUsed = true;
  }

  const reelDuration = [4, 6, 8].includes(Number(duration))
    ? Number(duration)
    : 4;

  try {
    const task = await runway.textToVideo.create({
      model: "gen4.5",
      promptText: prompt,
      ratio: "720:1280",
      duration: reelDuration,
      audio: false
    }).waitForTaskOutput();

    const videoUrl = task?.output?.[0];

    if (!videoUrl) {
      throw new Error("Runway returned no video URL");
    }

    res.json({
      status: "completed",
      message: "Real Runway Reel created successfully",
      taskId: task.id || "runway-" + Date.now(),
      videoUrl,
      premium: user.premium,
      watermark: "VIKASH MEHTA 8051",
      demo: false,
      duration: reelDuration
    });
  } catch (error) {
    console.error("RUNWAY ERROR:", error?.message || error);

    res.status(503).json({
      status: "failed",
      error: "RUNWAY_GENERATION_FAILED",
      message: error?.message || "Runway generation failed"
    });
  }
});
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
