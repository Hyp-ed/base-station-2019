#include <iostream>
#include <cerrno>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <thread>
#include "types/message.hpp"
#include "types/message.pb.h"

#define PORT 9090
#define SERVER_IP "localhost"
#define BUFFER_SIZE 1024

void Read(int sockfd) {
    // char buffer[BUFFER_SIZE];
//
    // while (true) {
        // std::memset(&buffer, 0, sizeof(buffer));
        // if (recv(sockfd, buffer, BUFFER_SIZE, 0) <= 0) {
            // std::cerr << "Error: " << strerror(errno) << std::endl;
            // exit(5);
        // }
//
        // std::cout << "FROM SERVER: " << buffer;
    // }



    char buffer[4]; // 32 bit size
    int bytecount = 0;

    std::memset(buffer, 0, sizeof(buffer));

    while (true) {
        if ((bytecount = recv(sockfd, buffer, 4, MSG_PEEK)) == -1) { // error
            std::cerr << "Error receiving data" << std::endl;
        }
        else if (bytecount == 0) { // empty/nothing/probs connection closed
            break;
        }

        std::cout << "First read byte count is: " << bytecount << std::endl;
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

    std::cout << "Waiting to connect to server..." << std::endl;

    // connect to the server
    if (connect(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
        std::cerr << "Error: " << strerror(errno) << std::endl;
        exit(3);
    }

    std::cout << "Connected to server" << std::endl;

    // start message reading thread to run in background
    std::thread threadObj(Read, sockfd);

    // send messages
    // char *msg = "hello from client\n";
    // int len = strlen(msg);
    // for (int i = 0; i < 10000000; i++) {
        // if (send(sockfd, msg, len, 0) < 0) {
            // std::cerr << "Error: " << strerror(errno) << std::endl;
            // exit(4);
        // }
    // }

    for (int i = 0; i < 1000000; i++) {
        types::message test(1, 222);
        test.send(sockfd);

        test = types::message(2, 444);
        test.send(sockfd);

        test = types::message(3, 888);
        test.send(sockfd);

        test = types::message(1, 223);
        test.send(sockfd);

        test = types::message(2, 445);
        test.send(sockfd);

        test = types::message(3, 889);
        test.send(sockfd);
    }

    // signify end of messaging
    types::message end_msg("END");
    end_msg.send(sockfd);

    // wait for message reading thread to finish
    threadObj.join();

    close(sockfd);
    return 0;
}
