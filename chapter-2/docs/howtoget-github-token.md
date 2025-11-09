# How to Get GitHub JWT Tokens

This guide explains how to obtain GitHub JWT tokens for testing your JWT authentication endpoint.

## What are GitHub JWT Tokens?

GitHub JWT tokens are JSON Web Tokens issued by GitHub in specific contexts, primarily:
- **GitHub Actions workflows** (`GITHUB_TOKEN`)
- **GitHub Apps** (installation access tokens in JWT format)
- **OIDC tokens** from GitHub Actions

These are different from GitHub Personal Access Tokens (PATs) which are not JWTs.

## 1. GitHub Actions OIDC Token (Recommended for Testing)

This is the easiest way to get a real GitHub JWT token.

### Steps:
1. Create a simple GitHub Actions workflow in your repository:

```yaml
# .github/workflows/get-jwt.yml
name: Get JWT Token
on:
  workflow_dispatch:  # Manual trigger

jobs:
  get-token:
    runs-on: ubuntu-latest
    permissions:
      id-token: write  # Required for OIDC
      contents: read
    steps:
      - name: Get OIDC Token
        run: |
          # Get the OIDC token
          OIDC_TOKEN=$(curl -s -H "Authorization: bearer $ACTIONS_ID_TOKEN_REQUEST_TOKEN" \
            "$ACTIONS_ID_TOKEN_REQUEST_URL&audience=https://github.com/muthuishere" | jq -r '.value')
          
          echo "Your GitHub JWT Token:"
          echo "$OIDC_TOKEN"
          
          # Decode the token to see claims (for debugging)
          echo "Token payload (base64 decoded):"
          echo "$OIDC_TOKEN" | cut -d. -f2 | base64 -d | jq .
```

2. Commit this workflow to your repository
3. Go to Actions tab and run the "Get JWT Token" workflow manually
4. Check the workflow logs to see your JWT token

### Usage:
```http
GET http://localhost:8080/api/user
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJyZXBvOm11dGh1aXNoZXJlL3JlcG8tcmVmOnJlZnMvaGVhZHMvbWFpbiI...
```

## 2. GitHub App JWT Token

### For GitHub Apps:
1. Create a GitHub App in [GitHub Settings > Developer settings > GitHub Apps](https://github.com/settings/apps)
2. Generate a private key for your app
3. Use the private key to create a JWT token
4. Exchange the JWT for an installation access token

### Example script to generate GitHub App JWT:
```python
import jwt
import time
from datetime import datetime, timedelta

# Your GitHub App details
app_id = "your_app_id"
private_key = """-----BEGIN RSA PRIVATE KEY-----
your_private_key_content_here
-----END RSA PRIVATE KEY-----"""

# Create JWT payload
payload = {
    'iat': int(time.time()),
    'exp': int(time.time()) + 600,  # 10 minutes
    'iss': app_id
}

# Generate JWT
token = jwt.encode(payload, private_key, algorithm='RS256')
print(token)
```

## 3. Manual JWT Creation (For Testing Only)

You can create a test JWT token using online tools or libraries:

### Using jwt.io:
1. Go to [jwt.io](https://jwt.io)
2. In the payload, add GitHub-like claims:
```json
{
  "sub": "repo:muthuishere/demo:ref:refs/heads/main",
  "aud": "https://github.com/muthuishere",
  "iss": "https://token.actions.githubusercontent.com",
  "login": "muthuishere",
  "id": "123456",
  "email": "your-email@example.com",
  "name": "Your Name",
  "avatar_url": "https://avatars.githubusercontent.com/u/123456",
  "iat": 1730550000,
  "exp": 1730553600
}
```
3. Generate the token (note: this won't be verifiable without proper signing)

## 4. Using GitHub CLI to check OIDC configuration

If you have GitHub CLI installed:

```bash
# Login to GitHub CLI
gh auth login

# Check OIDC subject customization (this just shows config, not the token)
gh api /repos/muthuishere/javafest/actions/oidc/customization/sub
# Returns: {"use_default": true} - means using default subject format
```

**Note**: The CLI command above only shows OIDC configuration, not the actual JWT token. To get the actual token, you need to run a GitHub Actions workflow.

## 5. Firebase Auth JWT Tokens (Alternative JWT Source)

Firebase Auth provides JWT ID tokens that you can use to test JWT authentication. This is often easier than GitHub tokens for testing.

### Setup Firebase Project:
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Enable Authentication → Sign-in method → Enable Email/Password or Google
4. Get your Firebase config from Project Settings

### Get Firebase JWT Token:

#### Method 1: Using Firebase Web SDK
```html
<!DOCTYPE html>
<html>
<head>
    <script src="https://www.gstatic.com/firebasejs/9.0.0/firebase-app.js"></script>
    <script src="https://www.gstatic.com/firebasejs/9.0.0/firebase-auth.js"></script>
</head>
<body>
    <script>
        import { initializeApp } from 'firebase/app';
        import { getAuth, signInWithEmailAndPassword, onAuthStateChanged } from 'firebase/auth';

        const firebaseConfig = {
            // Your Firebase config
        };

        const app = initializeApp(firebaseConfig);
        const auth = getAuth(app);

        // Sign in and get token
        signInWithEmailAndPassword(auth, 'user@example.com', 'password')
            .then((userCredential) => {
                return userCredential.user.getIdToken();
            })
            .then((token) => {
                console.log('Firebase JWT Token:', token);
                // Use this token in your API tests
            });
    </script>
</body>
</html>
```

#### Method 2: Using Firebase Admin SDK (Node.js)
```javascript
const admin = require('firebase-admin');

// Initialize with service account
const serviceAccount = require('./path/to/serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Create custom token for testing
const uid = 'test-user-123';
const additionalClaims = {
  email: 'test@example.com',
  name: 'Test User'
};

admin.auth().createCustomToken(uid, additionalClaims)
  .then((customToken) => {
    console.log('Custom token:', customToken);
    // Use this to sign in and get ID token
  });
```

#### Method 3: Using Firebase REST API
```bash
# Sign in with email/password to get ID token
curl -X POST 'https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=YOUR_API_KEY' \
-H 'Content-Type: application/json' \
-d '{
  "email": "user@example.com",
  "password": "password",
  "returnSecureToken": true
}'

# Response includes idToken (this is your JWT)
```

### Firebase JWT Token Structure:
Firebase JWT tokens contain claims like:
```json
{
  "iss": "https://securetoken.google.com/your-project-id",
  "aud": "your-project-id",
  "auth_time": 1730550000,
  "user_id": "firebase-user-id",
  "sub": "firebase-user-id",
  "iat": 1730550000,
  "exp": 1730553600,
  "email": "user@example.com",
  "email_verified": true,
  "firebase": {
    "identities": {
      "email": ["user@example.com"]
    },
    "sign_in_provider": "password"
  }
}
```

### Testing with Firebase JWT:
```http
GET http://localhost:8080/api/user
Authorization: Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IjFmODhiODE2M2M...
```

**Advantages of Firebase JWT for testing:**
- Easy to obtain programmatically
- Well-structured JWT claims
- Long expiration times
- No GitHub Actions workflow needed

## Testing Your JWT Token

Once you have a JWT token, you can:

### 1. Decode it to see the claims:
```bash
# Using online tool: paste your token at https://jwt.io
# Or using command line:
echo "YOUR_JWT_TOKEN" | cut -d. -f2 | base64 -d | jq .
```

### 2. Test with your API:
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/user
```

## JWT Token Format

JWT tokens have three parts separated by dots:
```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJyZXBvOm11dGh1aXNoZXJlL2RlbW86cmVmOnJlZnMvaGVhZHMvbWFpbiIsImF1ZCI6Imh0dHBzOi8vZ2l0aHViLmNvbS9tdXRodWlzaGVyZSIsImlzcyI6Imh0dHBzOi8vdG9rZW4uYWN0aW9ucy5naXRodWJ1c2VyY29udGVudC5jb20iLCJsb2dpbiI6Im11dGh1aXNoZXJlIiwiaWQiOiIxMjM0NTYiLCJlbWFpbCI6InlvdXItZW1haWxAZXhhbXBsZS5jb20iLCJuYW1lIjoiWW91ciBOYW1lIiwiYXZhdGFyX3VybCI6Imh0dHBzOi8vYXZhdGFycy5naXRodWJ1c2VyY29udGVudC5jb20vdS8xMjM0NTYiLCJpYXQiOjE3MzA1NTAwMDAsImV4cCI6MTczMDU1MzYwMH0.signature_here
```

- **Header**: Algorithm and token type
- **Payload**: Claims (user data, expiration, etc.)
- **Signature**: Cryptographic signature for verification

## Important Security Notes

⚠️ **Never commit tokens to your repository!**

- Store tokens in environment variables
- Use `.env` files (and add them to `.gitignore`)
- Use secure secret management in production
- Rotate tokens regularly
- Use minimum required permissions

## For Development Testing

### Option 1: Use the sample token in api-test.http
The `api-test.http` file already contains a sample JWT token for testing.

### Option 2: Create environment file
Create a `.env` file in your project root:

```bash
# .env file (add this to .gitignore!)
GITHUB_JWT_TOKEN=your_jwt_token_here
```

Then use it in your tests:

```http
GET http://localhost:8080/api/user
Authorization: Bearer {{$dotenv GITHUB_JWT_TOKEN}}
```

### Option 3: Quick test with a GitHub Actions workflow
This is the most reliable way to get a real GitHub JWT:

```yaml
# .github/workflows/test-jwt.yml
name: Test JWT
on: workflow_dispatch

jobs:
  test:
    runs-on: ubuntu-latest
    permissions:
      id-token: write
    steps:
      - name: Get JWT and test API
        run: |
          TOKEN=$(curl -s -H "Authorization: bearer $ACTIONS_ID_TOKEN_REQUEST_TOKEN" \
            "$ACTIONS_ID_TOKEN_REQUEST_URL&audience=https://github.com/muthuishere" | jq -r '.value')
          
          echo "Testing with JWT token..."
          curl -X GET http://your-api-url/api/user \
            -H "Authorization: Bearer $TOKEN"
```

## Troubleshooting

### Token not working?
1. Check if token has expired
2. Verify required scopes/permissions
3. Ensure token format is correct
4. Test token with GitHub API first: `curl -H "Authorization: Bearer YOUR_TOKEN" https://api.github.com/user`

### Common errors:
- `401 Unauthorized`: Invalid or expired token
- `403 Forbidden`: Token lacks required permissions
- `422 Unprocessable Entity`: Malformed request

## Next Steps

1. Get a GitHub Personal Access Token
2. Update the `api-test.http` file with your real token
3. Start your application: `./gradlew devSse`
4. Test the `/api/user` endpoint
5. Check the response to see your GitHub user information extracted from the JWT