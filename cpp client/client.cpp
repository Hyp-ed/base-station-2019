#include <iostream>
#include <cerrno>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <thread>

#define PORT 9090
#define SERVER_IP "localhost"
#define BUFFER_SIZE 8192

void Read(int sockfd) {
    char buffer[BUFFER_SIZE];

    while (true) {
        std::memset(&buffer, 0, sizeof(buffer));
        if (recv(sockfd, buffer, BUFFER_SIZE, 0) < 0) {
            std::cerr << "Error: " << strerror(errno) << std::endl;
            exit(5);
        }

        std::cout << "FROM SERVER: " << buffer;
    }
}

int main(int argc, char *argv[]) {
    int sockfd;
    struct sockaddr_in serv_addr; //struct containing an internet address (server in this case)
    struct hostent *server;

    // create socket
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        std::cerr << "Error: " << strerror(errno) << std::endl;
        exit(1);
    }

    // resolve host address (convert from symbolic name to IP)
    server = gethostbyname(SERVER_IP);
    if (server == NULL) {
        std::cerr << "Error: " << strerror(errno) << std::endl;
        exit(2);
    }

    // server address stuff
    std::memset(&serv_addr, 0, sizeof(serv_addr)); // initialize to zeroes
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(PORT);
    std::memcpy(&serv_addr.sin_addr.s_addr, server->h_addr_list[0], server->h_length);

    // connect to the server
    if (connect(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
        std::cerr << "Error: " << strerror(errno) << std::endl;
        exit(3);
    }

    // start message reading thread to run in background
    std::thread threadObj(Read, sockfd);

    // send messages
    char *msg = "hello from client\n";
    int len = strlen(msg);
    for (int i = 0; i < 10000000; i++) {
        if (send(sockfd, msg, len, 0) < 0) {
            std::cerr << "Error: " << strerror(errno) << std::endl;
            exit(4);
        }
    }

    // signify end of messaging
    send(sockfd, "END", 3, 0);
    send(sockfd, ".", 1, 0);

    // wait for message reading thread to finish
    threadObj.join();

    close(sockfd);
    return 0;
}
