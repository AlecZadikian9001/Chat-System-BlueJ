------------------------------------------------------------------------
This is the project README file. Here, you should describe your project.
Tell the reader (someone who does not know anything about this project)
all he/she needs to know. The comments should usually include at least:
------------------------------------------------------------------------

PROJECT TITLE: Chat System
PURPOSE OF PROJECT: Chat server with multiple rooms, admin ranks, audio chats, and more. Encrypts all packets sent between clients and server.
VERSION or DATE: September 2013
HOW TO START THIS PROJECT: Open the BlueJ package, start a new DualTester with static void main(String[] args) to test.
AUTHORS: Alec Zadikian, Varun Gadh
USER INSTRUCTIONS: None yet.

CLASS NOTES:

= Server =

ChatServerMain - Responsible for accepting new client connections and making threads for them. Also handles login. Stores the user, rooms, and audio chat databases, saving the users and rooms to files when the server closes. Has methods for various other server-wide things like audio chats, cross-room whispers, and user banishment.

ChatServerChatRoom - Holds client threads by id in a pseudo hash set. Involved in anything within chat rooms such as sending a message to everyone in the room.

ChatServerThread - Extends Thread. Each connected client gets a thread immediately. Negotiates login with ChatServerMain then lets the user chat or send commands. Has methods for accessing the user info.

UserAccount - Just stores information for a user.

AudioThread - Created every time an audio chat invitation is sent. Listens for 2 connections then acts as a relay server for the clients to exchange audio streams.

= Client =

ChatClient - View/controller class. Listens for input from user and listens for messages from the server. Responsible for knowing when to start audio client threads.

AudioClient - Extends Thread. Sends microphone input to server and receives sound from server.

In - Incoming message model.

Out - Outgoing message model.

= Common =

Encryptor - Includes static methods for encrypting/decrypting messages.

Finals - Just stores static final variables.

DualTester - Launches a server on port 9000 and three clients on separate threads to allow easy testing on one computer.