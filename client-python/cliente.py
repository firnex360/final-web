import grpc
import ShortUrlRn_pb2
import ShortUrlRn_pb2_grpc
from google.protobuf.timestamp_pb2 import Timestamp

def get_urls_by_user(stub, username):
    request = ShortUrlRn_pb2.UserRequests(username=username)
    try:
        response = stub.getUrlsByUser(request)
        print(f"URLs del usuario actual '{username}':")
        for url in response.urls:
            print(f"- Short: {url.shortUrl}, Original: {url.originalUrl}, Accesos: {url.accessCount}")
            print(f"  Logs:")
            for log in url.accessLogs:
                print(f"    IP: {log.ip}, OS: {log.os}, Time: {log.accessTime.seconds}")
    except grpc.RpcError as e:
        print("Error:", e.details())

def create_short_url(stub, username, original_url):
    request = ShortUrlRn_pb2.CreateUrlRequests(
        username=username,
        originalUrl=original_url
    )
    try:
        response = stub.createShortUrlAPI(request)
        print("Short URL created:")
        print(f"- URL Original: {response.originalUrl}")
        print(f"- Short URL: {response.shortUrl}")
        print(f"- Creado en: {response.createdAt}")
        print(f"- Preview (base64): {response.previewImageBase64[:30]}...")  #CHECK
    except grpc.RpcError as e:
        print("Error:", e.details())

def main():
    with grpc.insecure_channel("localhost:5000") as channel:
        stub = ShortUrlRn_pb2_grpc.ShortUrlRnStub(channel)

        # Replace with your test data
        get_urls_by_user(stub, username="admin")

        create_short_url(
            stub,
            username="admin",
            original_url="https://www.google.com"
        )

if __name__ == "__main__":
    main()