"use strict";

const buttons = {
    make: document.getElementById("make"),
    copy: document.getElementById("copy"),
    accept: document.getElementById("accept"),
    send: document.getElementById("send"),
    close: document.getElementById("close"),
};
buttons.copy.disabled = true;
buttons.accept.disabled = true;
buttons.send.disabled = true;
buttons.close.disabled = true;
const texts = {
    offer: document.getElementById("offer"),
    answer: document.getElementById("answer"),
    message: document.getElementById("message"),
    logs: document.getElementById("logs"),
};
texts.offer.readOnly = true;
texts.logs.readOnly = true;

let ctx = null;
function newPC() {
    texts.logs.value = ``;
    const pc = new RTCPeerConnection({iceServers: [
        {url: "stun:stun.services.mozilla.com:3478"},
    ]});
    pc.addEventListener("datachannel", ev => {
        // receiver channel from remote
        console.log("datachannel", ev);
        ev.channel.addEventListener("message", ev => {
            const message = ev.data;
            texts.logs.value += `[answer] ${message}\n`;
        }, false);
        ev.channel.addEventListener("close", ev => {
            // NOTE: close event is not in RTCPeerConnection
            console.log("close");
            texts.logs.value += `[closed]\n`;
            buttons.make.disabled = false;
            buttons.copy.disabled = true;
            buttons.accept.disabled = true;
            buttons.send.disabled = buttons.close.disabled = true;
            texts.offer.value = texts.answer.value = "";
            ctx = null;
        }, false);
        buttons.send.disabled = buttons.close.disabled = false;
    }, false);
    // sender channel to remote
    const dc = pc.createDataChannel("offer");
    return {pc, dc};
}

// 1. make offer
buttons.make.addEventListener("click", ev => {
    console.log("make");
    ctx = newPC();
    ctx.pc.addEventListener("icecandidate", ev => {
        console.log("icecandidate", ev);
        if (!ev.candidate) {
            // use SDP text with all candidate
            texts.offer.value = ctx.pc.localDescription.sdp;
            buttons.copy.disabled = false;
            buttons.accept.disabled = false;
        }
    }, false);
    ctx.pc.createOffer().then(offer => {
        console.log("offer", offer);
        ctx.pc.setLocalDescription(offer);
    }).catch(console.error);
    buttons.make.disabled = true;
}, false);
buttons.copy.addEventListener("click", ev => {
    texts.offer.select();
    document.execCommand("copy");
}, false);

// 4. accept answer
buttons.accept.addEventListener("click", ev => {
    const answer = new RTCSessionDescription({
        type: "answer", sdp: texts.answer.value,
    });
    ctx.pc.setRemoteDescription(answer);
    buttons.accept.disabled = true;
}, false);

// 5. messaging 
buttons.send.addEventListener("click", ev => {
    const message = texts.message.value;
    texts.logs.value += `[offer] ${message}\n`;    
    ctx.dc.send(message);
}, false);

// 6. close connection
buttons.close.addEventListener("click", ev => {
    ctx.pc.close();
}, false);
