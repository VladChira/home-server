import whisper

model = whisper.load_model("turbo")
result = model.transcribe("audio2.m4a")
print(result["text"])