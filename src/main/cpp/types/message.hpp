#include <string>
#include <sys/socket.h>

namespace types {
    class message {
        public:
            std::string command;
            std::string data;

            message(std::string d) {
                this->command = "\n"; // basically empty
                this->data = d + '\n';
            }

            message(int c, int d) {
                this->command = std::to_string(c) += '\n';
                this->data = std::to_string(d) += '\n';
            }

            int send(int socket) {
                const char *cmd = (this->command).c_str();
                const char *d = (this->data).c_str();

                int sent_cmd = ::send(socket, cmd, strlen(cmd), 0); // send command
                int sent_d = ::send(socket, d, strlen(d), 0); // send data

                if (sent_cmd == -1 || sent_d == -1) {
                    return -1;
                }
                else {
                    return sent_cmd + sent_d;
                }
            }
    };
}
