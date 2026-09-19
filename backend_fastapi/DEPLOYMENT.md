# Production deployment checklist

1. Set `ENVIRONMENT=production`, `DEBUG_MODE=false`, a unique 32+ character `JWT_SECRET`, and an explicit `CORS_ORIGINS` value.
2. Use a managed PostgreSQL database. Run schema migrations in CI/deployment; do not rely on the startup compatibility migration for production changes.
3. Store Firebase, Cloudinary, Razorpay, and database credentials in the deployment secret store. Never use the example values.
4. Deploy behind HTTPS with health checks at `/api/health`. Build Android release with `-PPROD_API_BASE_URL=https://api.your-domain.tld/api/v1/`, a real Razorpay public key, and the matching Firebase configuration.
5. Configure Razorpay webhooks and independently verify their signature before changing booking/payment status.
6. Run `python -m pytest tests -q` in CI before deploy. Test Firebase and Razorpay against their official sandbox accounts before release.
