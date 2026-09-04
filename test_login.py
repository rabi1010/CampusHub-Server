import http.client
import json

def test_login():
    conn = http.client.HTTPConnection("localhost", 8080)
    payload = json.dumps({
        "email": "admin@campushub.edu",
        "password": "admin123"
    })
    headers = {
        'Content-Type': 'application/json'
    }
    
    print(f"Attempting login to http://localhost:8080/api/auth/login...")
    try:
        conn.request("POST", "/api/auth/login", payload, headers)
        response = conn.getresponse()
        data = response.read().decode()
        
        print(f"Status: {response.status}")
        print(f"Response: {data}")
        
        if response.status == 200:
            print("✅ Login Successful!")
        else:
            print(f"❌ Login Failed with status {response.status}")
            
    except Exception as e:
        print(f"❌ Error connecting to backend: {e}")
    finally:
        conn.close()

if __name__ == "__main__":
    test_login()
