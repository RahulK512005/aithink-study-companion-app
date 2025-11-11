==========================================
  GEMMA 2B MODEL DOWNLOAD INSTRUCTIONS
==========================================

📥 REQUIRED: Download the AI Model File

Your app needs the Gemma 2B model file to work. Follow these steps:

1. GO TO KAGGLE:
   Visit: https://www.kaggle.com/models/google/gemma/tfLite/gemma-2b-it-gpu-int4

2. SIGN IN:
   - Create a free Kaggle account if you don't have one
   - Accept the Gemma model terms of use

3. DOWNLOAD THE MODEL:
   - Click the "Download" button
   - File name: gemma-2b-it-gpu-int4.bin
   - File size: ~1.5 GB
   - Download time: 5-15 minutes (depending on internet speed)

4. PLACE THE FILE HERE:
   Copy the downloaded file to THIS directory:
   
   AiThinkStudyCompanion/app/src/main/assets/gemma-2b-it-gpu-int4.bin
   
   The file should be in the SAME folder as this README.

5. VERIFY:
   - File name: gemma-2b-it-gpu-int4.bin (exact name)
   - File size: ~1.5 GB
   - Location: app/src/main/assets/

6. BUILD & RUN:
   After placing the model file:
   - Rebuild the app: gradlew clean assembleDebug
   - Install: gradlew installDebug
   - Launch and enjoy offline AI!

==========================================

⚠️  IMPORTANT NOTES:

- The model file is NOT included in the repository due to its large size (1.5 GB)
- You MUST download it manually from Kaggle
- Without the model file, the app will show an error message
- The model will be copied to cache on first launch (takes 30-60 seconds)
- Subsequent launches will be faster (5-10 seconds)

==========================================

🔧 TROUBLESHOOTING:

Q: Where exactly do I put the file?
A: In the same folder as this README file:
   app/src/main/assets/gemma-2b-it-gpu-int4.bin

Q: Can I use a different model?
A: Yes! Update the MODEL_FILENAME constant in OfflineAIService.kt

Q: The app says "Model file not found"?
A: Make sure:
   1. The file is in app/src/main/assets/
   2. The file name is EXACTLY: gemma-2b-it-gpu-int4.bin
   3. You rebuilt the app after adding the file

Q: Can I test without downloading the model?
A: The app will run but show an error in the chat panel

==========================================

📚 MORE INFO:

See MEDIAPIPE_INTEGRATION_GUIDE.md in the project root for:
- Detailed integration guide
- Alternative models
- Performance specs
- Full troubleshooting guide

==========================================

Happy Learning! 🚀
