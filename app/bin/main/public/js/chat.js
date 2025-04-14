// Connect to WebSocket
const socket = new WebSocket("ws://localhost:1000/chat");

// Listen for messages
socket.onmessage = function(event) {

    let data = JSON.parse(event.data); // Parse JSON
    let chatBox = document.getElementById("chat-messages");
    let message = document.createElement("p");

    // Determine color based on sender
    let senderUsername = data.senderId;
    let text = data.text;
    let usernameColor  = "blue"; 
    let messageColor = "black";

    message.innerHTML = `<strong style="color:${usernameColor}">${senderUsername}</strong>: <span style="color:${messageColor}">${text}</span>`;
    chatBox.append(message);
};

// Send message function
function sendMessageNew() {
    let input = document.getElementById("chat-input");
    let message = input.value.trim();

    if (message !== "") {
        let chatMessages = document.getElementById("chat-messages");
        chatMessages.innerHTML += "<p><strong>You:</strong> " + message + "</p>";
        //socket.send(message); // Send message through WebSocket
        socket.send(JSON.stringify({ type: "sendMessage", text: message }));
        input.value = "";
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
}

// function to retrieve user chats 
// function loadChatsForUser() {
//     let chatList = document.getElementById("chat-list");
//     chatList.innerHTML = ""; // Clear existing chats

//     // Mock chat data (replace with server response)
//     let chats = [
//         { id: 1, name: "John Doe" },
//         { id: 2, name: "Admin" },
//         { id: 3, name: "Alice" }
//     ];

//     // Add each chat as a clickable item
//     chats.forEach(chat => {
//         let chatItem = document.createElement("div");
//         chatItem.innerText = chat.name;
//         chatItem.classList.add("chat-item");
//         chatItem.onclick = function () {
//             openChat(chat.id, chat.name);
//         };
//         chatList.appendChild(chatItem);
//     });
// }

// // Function to open a chat and load messages
// function openChat(chatId, chatName) {
//     document.getElementById("chat-list").style.display = "none"; 
//     document.getElementById("chat-messages-container").style.display = "block";

//     // Set chat title
//     let chatMessages = document.getElementById("chat-messages");
//     chatMessages.innerHTML = `<h3>${chatName}</h3>`;

//     // Fetch messages for this chat (mocked)
//     let messages = [
//         { sender: "You", text: "Hello" },
//         { sender: chatName, text: "Hey there!" }
//     ];

//     // Display messages
//     messages.forEach(msg => {
//         let messageElement = document.createElement("p");
//         messageElement.innerHTML = `<strong style="color: blue">${msg.sender}</strong>: <span style="color: black">${msg.text}</span>`;
//         chatMessages.appendChild(messageElement);
//     });
// }

// // Function to go back to chat list
// function backToChatList() {
//     document.getElementById("chat-list").style.display = "block";
//     document.getElementById("chat-messages-container").style.display = "none";
// }

// // Load chats when the chatbox opens
// document.addEventListener("DOMContentLoaded", loadChatsForUser);
