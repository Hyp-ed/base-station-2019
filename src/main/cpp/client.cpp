#include <iostream>
#include <cerrno>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <thread>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl_lite.h>
#include <google/protobuf/message_lite.h>
#include "types/message.hpp"
#include "types/message.pb.h"

#define PORT 9090
#define SERVER_IP "localhost"

// reads header containing size encoded as varint, returns this size
google::protobuf::uint32 readHeader(char *buffer) {
    using namespace google::protobuf::io;

    google::protobuf::uint32 size; // this will hold the size of our message body in bytes
    ArrayInputStream raw_input(buffer, 4); // create raw stream containing buffer of varint
    CodedInputStream coded_input_ptr(&raw_input); // create CodedInput wrapper

    coded_input_ptr.ReadVarint32(&size); // read size as varint
    std::cout << "size of message: " << size << std::endl;

    return size;
}

// read body of a message, and print out the contents of this body
void readBody(int sockfd, google::protobuf::uint32 size) {
    using namespace google::protobuf::io;

    int bytes_received;
    char buffer[size + 4];

    // read whole message (header + body) into buffer
    if ((bytes_received = recv(sockfd, (void *) buffer, size + 4, 0)) == -1) {
        std::cerr << "Error receiving data (reading body)" << std::endl;
    }

    ArrayInputStream raw_input(buffer, size + 4); // raw input stream
    CodedInputStream coded_input_ptr(&raw_input); // CodedInput wrapper

    // we have to read size of message again bc buffer contains header + body (move file position indicator)
    // shouldn't change value of uint32 size variable we were passed in
    coded_input_ptr.ReadVarint32(&size); 

    CodedInputStream::Limit msg_limit = coded_input_ptr.PushLimit(size); // add limit to prevent reading beyond message length
    protoTypes::TestMessage msg;
    msg.ParseFromCodedStream(&coded_input_ptr); // deserialize
    coded_input_ptr.PopLimit(msg_limit); // remove limit

    std::cout << "FROM SERVER: " << msg.data() << std::endl;
}

void Read(int sockfd) {
    char buffer[4]; // 32 bit size
    int bytes_received = 0; // we use this so we can see if we are sent nothing/empty or an error occurs

    std::memset(buffer, 0, sizeof(buffer));

    while (true) {
        // below we read first four bytes of message into buffer, buffer should contain size varint
        if ((bytes_received = recv(sockfd, buffer, 4, MSG_PEEK)) == -1) { // error
            std::cerr << "Error receiving data (reading header)" << std::endl;
        }
        else if (bytes_received == 0) { // empty/nothing/probs connection closed
            break;
        }

        readBody(sockfd, readHeader(buffer));
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
