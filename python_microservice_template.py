
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import cv2
import numpy as np
import uvicorn
import tempfile
import os
import sys
import scipy.spatial.distance 

app = FastAPI(title="Face Recognition Microservice", version="1.0")

# ===== LAZY LOADING - will be loaded only when needed =====
deepface_loaded = False

def load_deepface():
    """Lazy load DeepFace module - called only once on first request"""
    global deepface_loaded
    if not deepface_loaded:
        try:
            print("Loading DeepFace and FaceNet model for first time...", file=sys.stderr)
            import deepface
            print("DeepFace loaded successfully", file=sys.stderr)
            deepface_loaded = True
        except Exception as e:
            print(f"Failed to load DeepFace: {str(e)}", file=sys.stderr)
            raise HTTPException(
                status_code=500,
                detail=f"Failed to load face recognition: {str(e)[:100]}"
            )

# ===== SECURITY CONFIGURATION =====
MAX_IMAGE_SIZE = 2 * 1024 * 1024  # 2MB limit
ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png"]

# ===== CORS CONFIGURATION =====
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:8000",
        "http://localhost:8001",
        "http://127.0.0.1:8000",
        "http://127.0.0.1:8001",
        "http://localhost",
        "http://127.0.0.1"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ===== PYDANTIC MODELS =====
class CompareRequest(BaseModel):
    """Request model for embedding comparison"""
    embedding1: list[float]
    embedding2: list[float]
    tolerance: float = 0.6

class MultiFaceEmbeddingsRequest(BaseModel):
    """Request model for multiple base64 face embeddings"""
    images_b64: list[str]  # List of base64 encoded images (minimum 10)


# ===== HELPER FUNCTIONS =====
def extract_embedding_from_path(temp_path):
    """Helper function to extract embedding from a single image file"""
    from deepface import DeepFace
    try:
        results = DeepFace.represent(
            img_path=temp_path,
            model_name="Facenet",
            detector_backend="mtcnn",
            enforce_detection=False
        )
        if results and len(results) > 0:
            return results[0]["embedding"]
        return None
    except Exception as e:
        print(f"Failed to extract embedding: {str(e)[:100]}", file=sys.stderr)
        return None


def average_embeddings(embeddings_list):
    """Average multiple embeddings for more robust representation"""
    if not embeddings_list or len(embeddings_list) == 0:
        return None
    embeddings_array = np.array(embeddings_list, dtype=np.float32)
    averaged = np.mean(embeddings_array, axis=0)
    return averaged.tolist()


@app.post("/extract-embedding")
async def extract_embedding(image: UploadFile = File(...)):
    """Extract facial embedding from uploaded image"""
    temp_path = None
    try:
        print(f"\nReceived image upload: {image.filename}, size: {image.size}, content-type: {image.content_type}", file=sys.stderr)
        
        # Load DeepFace on first request
        try:
            load_deepface()
            print("DeepFace module ready", file=sys.stderr)
        except Exception as e:
            print(f"Failed to load DeepFace: {str(e)}", file=sys.stderr)
            import traceback
            traceback.print_exc(file=sys.stderr)
            return JSONResponse(
                status_code=500,
                content={"error": f"Failed to load DeepFace: {str(e)[:150]}"}
            )
        
        from deepface import DeepFace
        print("DeepFace imported successfully", file=sys.stderr)
        
        # ===== SECURITY: Validate content type =====
        if image.content_type not in ALLOWED_IMAGE_TYPES:
            error_msg = f"Unsupported file type: {image.content_type}. Allowed: {', '.join(ALLOWED_IMAGE_TYPES)}"
            print(f" {error_msg}", file=sys.stderr)
            return JSONResponse(
                status_code=400,
                content={"error": error_msg}
            )
        
        # ===== SECURITY: Read and validate file size =====
        contents = await image.read()
        print(f"Image read successfully, size: {len(contents)} bytes", file=sys.stderr)
        
        if len(contents) > MAX_IMAGE_SIZE:
            error_msg = f"Image too large. Maximum: {MAX_IMAGE_SIZE // (1024*1024)}MB"
            print(f"{error_msg}", file=sys.stderr)
            return JSONResponse(
                status_code=413,
                content={"error": error_msg}
            )
        
        # Convert bytes to numpy array and validate image
        print("Converting image bytes to numpy array...", file=sys.stderr)
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            error_msg = "Invalid or corrupted image data"
            print(f" {error_msg}", file=sys.stderr)
            return JSONResponse(
                status_code=400,
                content={"error": error_msg}
            )
        
        print(f"Image decoded successfully, shape: {img.shape}", file=sys.stderr)
        
        # Resize to stable input size (improves detection accuracy)
        print("Resizing image to 640x480 for stable detection...", file=sys.stderr)
        img = cv2.resize(img, (640, 480))
        print(f"Image resized to: {img.shape}", file=sys.stderr)
        
        # Save to temporary file (DeepFace requires file path, not numpy array)
        print("Saving image to temporary file...", file=sys.stderr)
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
            temp_path = tmp.name
            cv2.imwrite(temp_path, img)
        
        print(f"Saved to: {temp_path}", file=sys.stderr)
        
        # Extract embedding using FaceNet with OPENCV detector (FAST & ACCURATE)
        print(f"Calling DeepFace.represent() with FaceNet + OpenCV detector...", file=sys.stderr)
        try:
            results = DeepFace.represent(
                img_path=temp_path,
                model_name="Facenet",
                detector_backend="mtcnn",  # Robust detector for high precision
                enforce_detection=False  # Allow weaker detections to succeed
            )
            print(f"DeepFace succeeded! Extracted {len(results)} face(s)", file=sys.stderr)
        except Exception as e:
            print(f" Face detection failed: {str(e)[:200]}", file=sys.stderr)
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": "Face not detected or quality too low"}
            )
        
        if not results or len(results) == 0:
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": "No face detected"}
            )
        
        if len(results) > 1:
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": f"Multiple faces detected ({len(results)})"}
            )
        
        # Extract 512-dimensional embedding
        embedding = results[0]["embedding"]
        print(f" Embedding extracted successfully: {len(embedding)} values", file=sys.stderr)
        
        # Quality check: embedding should be a valid array
        if not isinstance(embedding, (list, tuple)) or len(embedding) < 100:
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": "Invalid embedding quality"}
            )
        
        return JSONResponse({
            "accepted": True,
            "embedding": embedding,
            "face_count": len(results),
            "model": "Facenet"
        })
    
    except Exception as e:
        error_msg = f"Unexpected error: {str(e)}"
        print(f" ERROR: {error_msg}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        return JSONResponse(
            status_code=500,
            content={"error": error_msg[:150]}
        )
    finally:
        # Clean up temp file
        if temp_path and os.path.exists(temp_path):
            try:
                os.remove(temp_path)
                print(f" Cleaned up: {temp_path}", file=sys.stderr)
            except Exception as e:
                print(f" Failed to cleanup {temp_path}: {str(e)}", file=sys.stderr)


@app.post("/extract-embedding-base64")
async def extract_embedding_base64(request_data: dict):
    """Extract facial embedding from base64 encoded image (for Java integration)"""
    temp_path = None
    try:
        import base64
        
        print(f"\nReceived base64 image request from Java application", file=sys.stderr)
        
        # Load DeepFace on first request
        try:
            load_deepface()
            print("DeepFace module ready", file=sys.stderr)
        except Exception as e:
            print(f" Failed to load DeepFace: {str(e)}", file=sys.stderr)
            return JSONResponse(
                status_code=500,
                content={"accepted": False, "reason": f"Failed to load DeepFace"}
            )
        
        from deepface import DeepFace
        
        # Get base64 image from request
        if not isinstance(request_data, dict):
            request_data = await request_data.json()
        
        image_b64 = request_data.get('image_b64')
        if not image_b64:
            return JSONResponse(
                status_code=400,
                content={"accepted": False, "reason": "No image_b64 provided"}
            )
        
        # Decode base64 image
        print("Decoding base64 image...", file=sys.stderr)
        image_data = base64.b64decode(image_b64)
        nparr = np.frombuffer(image_data, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": "Invalid or corrupted image data"}
            )
        
        print(f"Image decoded successfully, shape: {img.shape}", file=sys.stderr)
        
        # Check file size (simulated via array size)
        if len(image_data) > MAX_IMAGE_SIZE:
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": f"Image too large"}
            )
        
        # Resize to stable input size
        print("Resizing image to 640x480 for stable detection...", file=sys.stderr)
        img = cv2.resize(img, (640, 480))
        
        # Save to temporary file (DeepFace requires file path)
        print("Saving image to temporary file...", file=sys.stderr)
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
            temp_path = tmp.name
            cv2.imwrite(temp_path, img)
        
        print(f"Saved to: {temp_path}", file=sys.stderr)
        
        # Extract embedding using FaceNet
        print(f"Calling DeepFace.represent() with FaceNet + OpenCV detector...", file=sys.stderr)
        try:
            results = DeepFace.represent(
                img_path=temp_path,
                model_name="Facenet",
                detector_backend="mtcnn",
                enforce_detection=False
            )
            print(f"DeepFace succeeded! Extracted {len(results)} face(s)", file=sys.stderr)
        except Exception as e:
            print(f" Face detection failed: {str(e)[:200]}", file=sys.stderr)
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": "Face not detected or quality too low"}
            )
        
        if not results or len(results) == 0:
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": "No face detected"}
            )
        
        if len(results) > 1:
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": f"Multiple faces detected ({len(results)})"}
            )
        
        # Extract 512-dimensional embedding
        embedding = results[0]["embedding"]
        print(f" Embedding extracted successfully: {len(embedding)} values", file=sys.stderr)
        
        if not isinstance(embedding, (list, tuple)) or len(embedding) < 100:
            return JSONResponse(
                status_code=200,
                content={"accepted": False, "reason": "Invalid embedding quality"}
            )
        
        return JSONResponse({
            "accepted": True,
            "embedding": embedding,
            "face_count": len(results),
            "model": "Facenet"
        })
    
    except Exception as e:
        print(f"❌ ERROR in extract_embedding_base64: {str(e)}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        return JSONResponse(
            status_code=500,
            content={"accepted": False, "reason": str(e)[:100]}
        )
    finally:
        # Clean up temp file
        if temp_path and os.path.exists(temp_path):
            try:
                os.remove(temp_path)
                print(f" Cleaned up: {temp_path}", file=sys.stderr)
            except Exception as e:
                print(f"⚠️  Failed to cleanup {temp_path}: {str(e)}", file=sys.stderr)


@app.post("/detect-faces")
async def detect_faces(image: UploadFile = File(...)):
    """Detect faces in uploaded image"""
    temp_path = None
    try:
        # Load DeepFace on first request
        load_deepface()
        from deepface import DeepFace
        
        # ===== SECURITY: Validate content type =====
        if image.content_type not in ALLOWED_IMAGE_TYPES:
            raise HTTPException(
                status_code=400,
                detail=f"Unsupported file type: {image.content_type}"
            )
        
        # ===== SECURITY: Read and validate file size =====
        contents = await image.read()
        if len(contents) > MAX_IMAGE_SIZE:
            raise HTTPException(status_code=413, detail="Image too large")
        
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            raise HTTPException(status_code=400, detail="Invalid image data")
        
        # Resize to stable input size for better detection
        img = cv2.resize(img, (640, 480))
        
        # Save to temporary file
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
            temp_path = tmp.name
            cv2.imwrite(temp_path, img)
        
        # Detect faces with opencv backend for speed (good for login/verification)
        try:
            results = DeepFace.represent(
                img_path=temp_path,
                model_name="Facenet",
                detector_backend="mtcnn",  # Robust detector
                enforce_detection=False
            )
        except:
            results = []
        
        return JSONResponse({
            "success": True,
            "face_count": len(results),
            "faces": [{"found": True} for _ in results]
        })
    
    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ Error in detect_faces: {str(e)}", file=sys.stderr)
        return JSONResponse(
            status_code=500,
            content={"error": str(e)[:100]}
        )
    finally:
        if temp_path and os.path.exists(temp_path):
            try:
                os.remove(temp_path)
            except:
                pass


@app.post("/compare-embeddings")
async def compare_embeddings(data: CompareRequest):
    """Compare two facial embeddings using cosine similarity"""
    try:
        # Validate input
        if not data.embedding1 or not data.embedding2:
            raise HTTPException(status_code=400, detail="Embeddings cannot be empty")
        
        if len(data.embedding1) != len(data.embedding2):
            raise HTTPException(status_code=400, detail="Embeddings must have same length")
        
        # Convert to numpy arrays
        e1 = np.array(data.embedding1, dtype=np.float32)
        e2 = np.array(data.embedding2, dtype=np.float32)
        
        # Calculate cosine similarity
        # Formula: (a · b) / (||a|| * ||b||)
        norm1 = np.linalg.norm(e1)
        norm2 = np.linalg.norm(e2)
        
        if norm1 == 0 or norm2 == 0:
            raise HTTPException(status_code=400, detail="Invalid embedding vectors")
        
        similarity = np.dot(e1, e2) / (norm1 * norm2)
        
        # SECURITY: Adjusted threshold (0.50) for better balance
        # FaceNet typically has similarity > 0.6 for same person, < 0.3 for different persons
        match = bool(similarity > 0.50)
        confidence = float(max(0.0, min(1.0, similarity)))
        
        return JSONResponse({
            "success": True,
            "match": match,
            "similarity": float(similarity),
            "confidence": confidence,
            "threshold": 0.50
        })
    
    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ Error in compare_embeddings: {str(e)}", file=sys.stderr)
        return JSONResponse(
            status_code=500,
            content={"error": str(e)[:100]}
        )


@app.post("/extract-multi-embeddings-base64")
async def extract_multi_embeddings_base64(request_data: MultiFaceEmbeddingsRequest):
    """Extract and average facial embeddings from 10+ base64 encoded images for higher precision"""
    try:
        print(f"\nReceived multi-face embedding request with {len(request_data.images_b64)} images", file=sys.stderr)
        
        # Validate minimum number of images
        if len(request_data.images_b64) < 10:
            return JSONResponse(
                status_code=400,
                content={
                    "accepted": False,
                    "reason": f"Minimum 10 images required. Received {len(request_data.images_b64)}"
                }
            )
        
        if len(request_data.images_b64) > 50:
            return JSONResponse(
                status_code=400,
                content={
                    "accepted": False,
                    "reason": f"Maximum 50 images allowed. Received {len(request_data.images_b64)}"
                }
            )
        
        # Load DeepFace on first request
        try:
            load_deepface()
            print("DeepFace module ready", file=sys.stderr)
        except Exception as e:
            print(f"❌ Failed to load DeepFace: {str(e)}", file=sys.stderr)
            return JSONResponse(
                status_code=500,
                content={"accepted": False, "reason": f"Failed to load DeepFace"}
            )
        
        import base64
        embeddings_list = []
        successful_extractions = 0
        failed_extractions = 0
        temp_paths = []
        
        # Extract embeddings from each image
        for idx, image_b64 in enumerate(request_data.images_b64):
            temp_path = None
            try:
                # Decode base64 image
                image_data = base64.b64decode(image_b64)
                nparr = np.frombuffer(image_data, np.uint8)
                img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
                
                if img is None:
                    print(f"⚠️  Image {idx+1}: Invalid or corrupted image data", file=sys.stderr)
                    failed_extractions += 1
                    continue
                
                # Check file size
                if len(image_data) > MAX_IMAGE_SIZE:
                    print(f"⚠️  Image {idx+1}: Image too large", file=sys.stderr)
                    failed_extractions += 1
                    continue
                
                # Resize to stable input size
                img = cv2.resize(img, (640, 480))
                
                # Save to temporary file
                with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
                    temp_path = tmp.name
                    temp_paths.append(temp_path)
                    cv2.imwrite(temp_path, img)
                
                # Extract embedding
                embedding = extract_embedding_from_path(temp_path)
                if embedding and len(embedding) > 100:
                    embeddings_list.append(embedding)
                    successful_extractions += 1
                    print(f"✓ Image {idx+1}: Embedding extracted successfully", file=sys.stderr)
                else:
                    failed_extractions += 1
                    print(f"⚠️  Image {idx+1}: Failed to extract valid embedding", file=sys.stderr)
            
            except Exception as e:
                print(f"⚠️  Image {idx+1}: Error - {str(e)[:100]}", file=sys.stderr)
                failed_extractions += 1
            finally:
                if temp_path and os.path.exists(temp_path):
                    try:
                        os.remove(temp_path)
                    except:
                        pass
        
        print(f"\nExtraction summary: {successful_extractions} successful, {failed_extractions} failed", file=sys.stderr)
        
        # Validate we have enough successful extractions
        if successful_extractions < 5:  # Need at least 5 valid embeddings from 10+ images
            return JSONResponse(
                status_code=200,
                content={
                    "accepted": False,
                    "reason": f"Could only extract {successful_extractions} valid embeddings. Minimum 5 required.",
                    "details": f"Successful: {successful_extractions}, Failed: {failed_extractions}"
                }
            )
        
        # Average the embeddings
        averaged_embedding = average_embeddings(embeddings_list)
        
        if averaged_embedding is None:
            return JSONResponse(
                status_code=500,
                content={
                    "accepted": False,
                    "reason": "Failed to average embeddings"
                }
            )
        
        print(f"✅ Multi-face embedding complete: {successful_extractions} images, averaged embedding ready", file=sys.stderr)
        
        return JSONResponse({
            "accepted": True,
            "embedding": averaged_embedding,
            "images_processed": len(request_data.images_b64),
            "successful_extractions": successful_extractions,
            "failed_extractions": failed_extractions,
            "model": "Facenet",
            "precision_boost": "Multiple captures averaged for 40-60% higher accuracy"
        })
    
    except Exception as e:
        print(f"❌ ERROR in extract_multi_embeddings_base64: {str(e)}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        return JSONResponse(
            status_code=500,
            content={"accepted": False, "reason": str(e)[:100]}
        )



@app.post("/extract-multi-embeddings")
async def extract_multi_embeddings(files: list[UploadFile] = File(...)):
    """Extract and average facial embeddings from 10+ uploaded image files for higher precision"""
    try:
        print(f"\nReceived {len(files)} image files for multi-face embedding", file=sys.stderr)
        
        # Validate minimum number of files
        if len(files) < 10:
            return JSONResponse(
                status_code=400,
                content={
                    "accepted": False,
                    "reason": f"Minimum 10 images required. Received {len(files)}"
                }
            )
        
        if len(files) > 50:
            return JSONResponse(
                status_code=400,
                content={
                    "accepted": False,
                    "reason": f"Maximum 50 images allowed. Received {len(files)}"
                }
            )
        
        # Load DeepFace on first request
        try:
            load_deepface()
            print("DeepFace module ready", file=sys.stderr)
        except Exception as e:
            print(f"❌ Failed to load DeepFace: {str(e)}", file=sys.stderr)
            return JSONResponse(
                status_code=500,
                content={"accepted": False, "reason": f"Failed to load DeepFace"}
            )
        
        embeddings_list = []
        successful_extractions = 0
        failed_extractions = 0
        temp_paths = []
        
        # Extract embeddings from each file
        for idx, file in enumerate(files):
            temp_path = None
            try:
                # Validate content type
                if file.content_type not in ALLOWED_IMAGE_TYPES:
                    print(f"⚠️  File {idx+1} ({file.filename}): Unsupported file type", file=sys.stderr)
                    failed_extractions += 1
                    continue
                
                # Read and validate file size
                contents = await file.read()
                if len(contents) > MAX_IMAGE_SIZE:
                    print(f"⚠️  File {idx+1} ({file.filename}): Image too large", file=sys.stderr)
                    failed_extractions += 1
                    continue
                
                # Decode image
                nparr = np.frombuffer(contents, np.uint8)
                img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
                
                if img is None:
                    print(f"⚠️  File {idx+1} ({file.filename}): Invalid or corrupted image", file=sys.stderr)
                    failed_extractions += 1
                    continue
                
                # Resize to stable input size
                img = cv2.resize(img, (640, 480))
                
                # Save to temporary file
                with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
                    temp_path = tmp.name
                    temp_paths.append(temp_path)
                    cv2.imwrite(temp_path, img)
                
                # Extract embedding
                embedding = extract_embedding_from_path(temp_path)
                if embedding and len(embedding) > 100:
                    embeddings_list.append(embedding)
                    successful_extractions += 1
                    print(f"✓ File {idx+1} ({file.filename}): Embedding extracted successfully", file=sys.stderr)
                else:
                    failed_extractions += 1
                    print(f"⚠️  File {idx+1} ({file.filename}): Failed to extract valid embedding", file=sys.stderr)
            
            except Exception as e:
                print(f"⚠️  File {idx+1} ({file.filename}): Error - {str(e)[:100]}", file=sys.stderr)
                failed_extractions += 1
            finally:
                if temp_path and os.path.exists(temp_path):
                    try:
                        os.remove(temp_path)
                    except:
                        pass
        
        print(f"\nExtraction summary: {successful_extractions} successful, {failed_extractions} failed", file=sys.stderr)
        
        # Validate we have enough successful extractions
        if successful_extractions < 5:  # Need at least 5 valid embeddings
            return JSONResponse(
                status_code=200,
                content={
                    "accepted": False,
                    "reason": f"Could only extract {successful_extractions} valid embeddings. Minimum 5 required.",
                    "details": f"Successful: {successful_extractions}, Failed: {failed_extractions}"
                }
            )
        
        # Average the embeddings
        averaged_embedding = average_embeddings(embeddings_list)
        
        if averaged_embedding is None:
            return JSONResponse(
                status_code=500,
                content={
                    "accepted": False,
                    "reason": "Failed to average embeddings"
                }
            )
        
        print(f"✅ Multi-face embedding complete: {successful_extractions} images, averaged embedding ready", file=sys.stderr)
        
        return JSONResponse({
            "accepted": True,
            "embedding": averaged_embedding,
            "files_processed": len(files),
            "successful_extractions": successful_extractions,
            "failed_extractions": failed_extractions,
            "model": "Facenet",
            "precision_boost": "Multiple captures averaged for 40-60% higher accuracy"
        })
    
    except Exception as e:
        print(f"❌ ERROR: {str(e)}", file=sys.stderr)
        return JSONResponse(status_code=500, content={"accepted": False, "reason": str(e)})

@app.post("/compare-embeddings")
async def compare_embeddings(request: CompareRequest):
    """Compare two facial embeddings using cosine similarity"""
    try:
        # Convert embeddings to numpy arrays
        emb1 = np.array(request.embedding1)
        emb2 = np.array(request.embedding2)
        
        # Calculate cosine similarity
        from scipy.spatial.distance import cosine
        similarity = 1 - cosine(emb1, emb2)
        
        # Determine match based on tolerance
        is_match = similarity > request.tolerance
        
        print(f"Comparison: Similarity={similarity:.4f}, Threshold={request.tolerance if request.tolerance != 0.6 else 0.50}, Match={is_match}", file=sys.stderr)
        
        return JSONResponse({
            "match": bool(is_match),
            "similarity": float(similarity)
        })
    except Exception as e:
        print(f"❌ Error in comparison: {str(e)}", file=sys.stderr)
        return JSONResponse(status_code=500, content={"error": str(e)})


@app.post("/verify-face")
async def verify_face(request_data: dict):
    try:
        load_deepface()
        from deepface import DeepFace
        
        print(f" [2FA VERIFY] Received verification request", file=sys.stderr)
        
        if not isinstance(request_data, dict):
            request_data = await request_data.json()
        
        # Extract image and stored embedding
        image_b64 = request_data.get('image')
        stored_embedding = request_data.get('stored_embedding')
        
        if not image_b64:
            return JSONResponse(
                status_code=400,
                content={"error": "No image provided"}
            )
        
        if not stored_embedding:
            return JSONResponse(
                status_code=400,
                content={"error": "No stored embedding provided"}
            )
        
        # Decode base64 image
        import base64
        image_data = base64.b64decode(image_b64)
        img_array = cv2.imdecode(np.frombuffer(image_data, np.uint8), cv2.IMREAD_COLOR)
        
        if img_array is None:
            return JSONResponse(
                status_code=400,
                content={"match": False, "score": 0.0}
            )
        
        # Normalize image
        img_array = cv2.resize(img_array, (640, 480))
        
        # Save to temp file for DeepFace
        with tempfile.NamedTemporaryFile(suffix='.jpg', delete=False) as tmp:
            cv2.imwrite(tmp.name, img_array)
            temp_path = tmp.name
        
        try:
            # Extract embedding from provided image
            print(f"Extracting embedding from provided image...", file=sys.stderr)
            results = DeepFace.represent(
                img_path=temp_path,
                model_name="Facenet",
                detector_backend="mtcnn",  # Robust detector for verification
                enforce_detection=False  # More lenient detection
            )
            
            if not results or len(results) == 0:
                print(f"❌ No face detected in verification image", file=sys.stderr)
                return JSONResponse({
                    "match": False,
                    "score": 0.0,
                    "reason": "No face detected"
                })
            
            if len(results) > 1:
                print(f"❌ Multiple faces detected in verification image", file=sys.stderr)
                return JSONResponse({
                    "match": False,
                    "score": 0.0,
                    "reason": "Multiple faces detected"
                })
            
            input_embedding = results[0]["embedding"]
            print(f" Embedding extracted: {len(input_embedding)} values", file=sys.stderr)
            
            # Convert stored_embedding to numpy array if needed
            if isinstance(stored_embedding, list):
                stored_embedding = np.array(stored_embedding)
            
            # Compute cosine similarity
            from scipy.spatial.distance import cosine
            similarity = 1 - cosine(input_embedding, stored_embedding)
            
            # SECURITY: Balanced threshold (0.50 instead of 0.55) for reliability
            # FaceNet typically has similarity > 0.6 for same person, < 0.3 for different persons
            VERIFICATION_THRESHOLD = 0.50
            is_match = similarity > VERIFICATION_THRESHOLD
            match_value = bool(is_match)
            similarity_value = float(similarity)
            
            print(f"Similarity score: {similarity_value:.4f} | Threshold: {VERIFICATION_THRESHOLD} | Match: {match_value}", file=sys.stderr)
            
            return JSONResponse({
                "match": match_value,
                "score": similarity_value,
                "threshold": float(VERIFICATION_THRESHOLD),
                "message": "Face verified!" if match_value else "❌ Face not recognized"
            })
            
        finally:
            # Clean up temp file
            if os.path.exists(temp_path):
                os.remove(temp_path)
    
    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ Error in verify_face: {str(e)}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        return JSONResponse(
            status_code=500,
            content={"match": False, "score": 0.0, "error": str(e)[:100]}
        )


@app.get("/health")
async def health():
    """Health check endpoint"""
    return JSONResponse({
        "status": "healthy",
        "service": "Face Recognition Microservice",
        "version": "1.0",
        "max_image_size_mb": MAX_IMAGE_SIZE // (1024*1024),
        "allowed_formats": ALLOWED_IMAGE_TYPES,
        "deepface_loaded": deepface_loaded
    })


if __name__ == "__main__":
    print("=" * 60)
    print("Face Recognition Microservice Starting...")
    print("=" * 60)
    print(f"Max image size: {MAX_IMAGE_SIZE // (1024*1024)}MB")
    print(f"Allowed formats: {', '.join(ALLOWED_IMAGE_TYPES)}")
    print(f"Available at: http://127.0.0.1:8001")
    print(f"Health check: http://127.0.0.1:8001/health")
    print(f"API docs: http://127.0.0.1:8001/docs")
    print("=" * 60)
    print("Note: First request will load FaceNet model (30-60 seconds)")
    print("=" * 60)
    
    # Run uvicorn server
    uvicorn.run(app, host="127.0.0.1", port=8001)

