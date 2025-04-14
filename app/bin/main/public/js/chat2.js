// WebSocket connection
const socket = new WebSocket("ws://localhost:1000/chat");

// Listen for messages from the server
socket.onmessage = function(event) {
    let data = JSON.parse(event.data);
    let chatBox = document.getElementById("chat-messages");

    if (data.type === "chatList") {
        displayChatList(data.chats);
    } else {

        let message = document.createElement("p");

        // Determine color based on sender
        let senderUsername = data.senderId;
        let text = data.text;
        let usernameColor  = "blue"; 
        let messageColor = "black";

        message.innerHTML = `<strong style="color:${usernameColor}">${senderUsername}</strong>: <span style="color:${messageColor}">${text}</span>`;
        chatBox.append(message);

        // chatBox.innerHTML = ""; // Clear old messages

        // let senderMessages = data.senderMessages || [];
        // let receiverMessages = data.receiverMessages || [];
        // let senderName = data.senderName || "You";
        // let receiverName = data.receiverName || "Other";

        // let messages = [];

        // senderMessages.forEach(msg => messages.push({ text: msg, sender: senderName }));
        // receiverMessages.forEach(msg => messages.push({ text: msg, sender: receiverName }));

        // // Display messages in chat order
        // messages.forEach(msg => {
        //     let messageElement = document.createElement("p");

        //     let usernameColor = msg.sender === senderName ? "blue" : "green";
        //     let messageColor = "black";

        //     messageElement.innerHTML = `<strong style="color:${usernameColor}">${msg.sender}</strong>: 
        //                                 <span style="color:${messageColor}">${msg.text}</span>`;
        //     chatBox.appendChild(messageElement);
        // });
    }
};

// socket.onmessage = function(event) {

//     let data = JSON.parse(event.data); // Parse JSON
//     let chatBox = document.getElementById("chat-messages");
//     let message = document.createElement("p");

//     // Determine color based on sender
//     let senderUsername = data.senderId;
//     let text = data.text;
//     let usernameColor  = "blue"; 
//     let messageColor = "black";

//     message.innerHTML = `<strong style="color:${usernameColor}">${senderUsername}</strong>: <span style="color:${messageColor}">${text}</span>`;
//     chatBox.append(message);
// };


function sendMessageNew(chatId) {
    let input = document.getElementById("chat-input");
    let message = input.value.trim();

    if (message !== "") {
        let chatMessages = document.getElementById("chat-messages");
        chatMessages.innerHTML += `<p><strong>You:</strong> ${message}</p>`;
        
        // Send message with chatId
        socket.send(JSON.stringify({ type: "sendMessage", chatId: chatId, text: message }));
        
        input.value = "";
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
}


// Display chat list in the UI
function displayChatList(chats) {
    let chatList = document.getElementById("chat-list");
    chatList.innerHTML = ""; // Clear existing chats

    chats.forEach(chat => {
        let chatItem = document.createElement("div");
        chatItem.innerText = chat.name;
        chatItem.classList.add("chat-item");
        chatItem.onclick = function () {
            openChat(chat.id, chat.name);
        };
        chatList.appendChild(chatItem);
    });
}

function openChat(chatId, chatName) {
    let chatMessagesContainer = document.getElementById("chat-messages-container");
    let chatListContainer = document.getElementById("chat-list"); // Assuming this holds the chat list
    let chatMessages = document.getElementById("chat-messages");

    // Show the chat messages section
    chatMessagesContainer.style.display = "block";
    
    // Hide the chat list (if you want the chat list to disappear)
    if (chatListContainer) chatListContainer.style.display = "none";

    // Clear previous messages and set the chat title
    chatMessages.innerHTML = `<h3>${chatName}</h3>`;

    // Send request to the server for this chat's messages
    socket.send(JSON.stringify({ type: "openChat", chatId: chatId }));
}

function backToChatList() {
    let chatMessagesContainer = document.getElementById("chat-messages-container");
    let chatListContainer = document.getElementById("chat-list-container");

    chatMessagesContainer.style.display = "none"; // Hide chat messages
    if (chatListContainer) chatListContainer.style.display = "block"; // Show chat list
}

