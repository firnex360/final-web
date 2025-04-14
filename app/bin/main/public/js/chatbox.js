function toggleChat() {
    let chatBody = document.getElementById("chat-body");
    chatBody.style.display = (chatBody.style.display === "none" || chatBody.style.display === "") ? "block" : "none";
}

function sendMessageOld() {
    let input = document.getElementById("chat-input");
    let message = input.value;
    if (message.trim() !== "") {
        let chatMessages = document.getElementById("chat-messages");
        chatMessages.innerHTML += "<p><strong>You:</strong> " + message + "</p>";
        input.value = "";
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
}
