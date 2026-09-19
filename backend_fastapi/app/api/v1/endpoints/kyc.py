from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app import models, schemas
from app.api.deps import get_db, get_current_user

router = APIRouter()

@router.post("/", response_model=schemas.KycDocumentResponse)
def upload_kyc_document(
    document: schemas.KycDocumentCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    new_doc = models.KycDocument(
        user_id=current_user.id,
        document_type=document.document_type,
        document_url=document.document_url,
        status=models.KycStatus.PENDING
    )
    db.add(new_doc)
    
    # Update user status
    current_user.kyc_status = models.KycStatus.UNDER_REVIEW
    
    db.commit()
    db.refresh(new_doc)
    return new_doc


@router.get("/my", response_model=List[schemas.KycDocumentResponse])
def get_my_kyc_documents(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    docs = db.query(models.KycDocument).filter(models.KycDocument.user_id == current_user.id).all()
    return docs
