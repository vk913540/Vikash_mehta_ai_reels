# VIKASH MEHTA AI REELS — Android starter

This is a starter/prototype for an Android AI Reel app with:

- One free Reel per local app install
- Premium/subscription screen
- Google Play Billing integration skeleton
- Google AdMob banner test ad at the bottom
- Dark UI inspired by modern AI reel apps

## Important

This project does NOT contain a real AI video-generation model/API. The `CREATE REEL` action currently simulates generation. Connect your own secure backend and AI video provider before production.

For production:
1. Create a Google Play subscription product with ID `premium_monthly`.
2. Replace the AdMob test app ID and banner unit ID with your own IDs.
3. Verify Play purchases on a backend before granting premium.
4. Move the free-generation counter to your backend/database so uninstall/reinstall cannot reset it.
5. Add authentication.
6. Add your AI video generation API on the server, never expose secret API keys in the Android app.

The Google test banner ID used here is intentionally a test ID.
