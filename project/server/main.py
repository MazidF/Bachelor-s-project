import socket
import Server


def get_local_ip():
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
        s.connect(("8.8.8.8", 80))  # Connect to a known public server
        ip = s.getsockname()[0]  # Get the IP of the current interface
    return ip


print(f"Local IP Address: {get_local_ip()}")

server = Server.Server(host=get_local_ip())
server.run()

