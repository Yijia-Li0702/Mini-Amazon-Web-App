import psycopg2
import socket
# import google
from google.protobuf.internal.encoder import _EncodeVarint
from google.protobuf.internal.decoder import _DecodeVarint32

# Get an open TCP socket(SOCK_STREAM) to the ip address
def connect_backend(address):
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect(address)
    return client_socket

def send_request(request, socket):
    serialized_request = request.SerializeToString()
    size = request.ByteSize()
    _EncodeVarint(socket.send, len(serialized_request), None)
    socket.send(serialized_request)
    # socket.sendall(_VarintBytes(size) + serialized_request)


# import socket

# def process(packege_id):
#     s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#     s.connect(, )
#     msg = str(package_id) + '\n'
#     s.send(msg.encode('utf-8'))
#     response = = s.recv(2048)
#     response = response.decode()
#     response = response.split(":")
#     if response[0] == "ack" and response[1] == str(package_id):
#         return True
#     return False