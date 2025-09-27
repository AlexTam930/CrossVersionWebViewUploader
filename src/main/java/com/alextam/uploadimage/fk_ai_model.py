# file: fk_ai_model.py is to show a exsample to apply local AI model in mobile end, and even PC end.
# So, it uses a fake AI model here to process files and data. 
# As long as you have a available local AI model, please consider replacing it with yours.

import random

def analyze_content(file_content_str: str) -> dict:
    """
    A fake AI model function that "analyzes" text content.
    In a real-world scenario, this would be replaced with calls to a real
    Large Language Model (LLM) API (like GPT, Gemini) or a locally hosted model.
    """
    # Simulate AI analysis by checking content length and picking keywords.
    word_count = len(file_content_str.split())
    
    summary = f"This is a fake AI-generated summary. The document contains approximately {word_count} words."
    
    possible_keywords = ["important", "confidential", "report", "image", "data", "analysis"]
    keywords = random.sample(possible_keywords, min(3, len(possible_keywords)))
    
    print(f"--- Fake AI Analysis Complete for content with {word_count} words. ---")

    return {
        "summary": summary,
        "keywords": keywords,
        "content_word_count": word_count
    }
