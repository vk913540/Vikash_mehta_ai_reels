import express from "express";
import cors from "cors";

const app = express();
app.use(cors());
app.use(express.json());

const users = new Map();
function getUser(id) {
  if (!users.has(id)) users.set(id, { freeReelUsed:false, premium:false });
  return users.get(id);
}

app.get("/health", (_,res) => res.json({ok:true, app:"VIKASH MEHTA AI REELS"}));

app.get("/v1/me/:userId", (req,res) => {
  res.json(getUser(req.params.userId));
});

app.post("/v1/reels/generate", async (req,res) => {
  const {userId,prompt} = req.body || {};
  if (!userId || !prompt) return res.status(400).json({error:"userId and prompt are required"});

  const user = getUser(userId);
  if (!user.premium && user.freeReelUsed) {
    return res.status(402).json({
      error:"FREE_REEL_USED",
      message:"Subscribe to create more reels."
    });
  }

  // Replace this demo URL with a real server-side AI video provider call.
  const demoUrl = "https://example.com/generated-reel.mp4";
  if (!user.premium) user.freeReelUsed = true;

  res.json({
    status:"completed",
    videoUrl:demoUrl,
    premium:user.premium,
    watermark:"VIKASH_MEHTA_8051"
  });
});

// Production: verify Google Play purchaseToken on the server,
// then grant premium only after successful verification.

app.listen(process.env.PORT || 8080);
