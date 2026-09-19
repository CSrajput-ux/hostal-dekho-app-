from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app import models, schemas
from app.api.deps import get_db, get_current_user

router = APIRouter()

@router.post("/{property_id}", response_model=schemas.WishlistResponse)
def add_to_wishlist(
    property_id: str,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    # Check if property exists
    prop = db.query(models.Property).filter(models.Property.id == property_id).first()
    if not prop:
        raise HTTPException(status_code=404, detail="Property not found")
        
    # Check if already in wishlist
    existing = db.query(models.UserPropertyWishlist).filter(
        models.UserPropertyWishlist.user_id == current_user.id,
        models.UserPropertyWishlist.property_id == property_id
    ).first()
    
    if existing:
        return existing
        
    new_wishlist = models.UserPropertyWishlist(
        user_id=current_user.id,
        property_id=property_id
    )
    db.add(new_wishlist)
    db.commit()
    db.refresh(new_wishlist)
    return new_wishlist


@router.delete("/{property_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_from_wishlist(
    property_id: str,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    existing = db.query(models.UserPropertyWishlist).filter(
        models.UserPropertyWishlist.user_id == current_user.id,
        models.UserPropertyWishlist.property_id == property_id
    ).first()
    
    if existing:
        db.delete(existing)
        db.commit()
        
    return None

@router.get("/my", response_model=List[schemas.PropertySearchItem])
def get_my_wishlist(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    # Join with properties
    wishlists = db.query(models.UserPropertyWishlist, models.Property).join(
        models.Property, models.UserPropertyWishlist.property_id == models.Property.id
    ).filter(
        models.UserPropertyWishlist.user_id == current_user.id
    ).all()
    
    result = []
    for _, prop in wishlists:
        # We need to map to PropertySearchItem
        item = schemas.PropertySearchItem(
            id=prop.id,
            name=prop.name,
            address=prop.address,
            city=prop.city,
            latitude=prop.latitude,
            longitude=prop.longitude,
            type=prop.type,
            gender=prop.gender,
            verified_badge=prop.verified_badge,
            images=prop.images,
            rating=prop.rating
        )
        result.append(item)
        
    return result
