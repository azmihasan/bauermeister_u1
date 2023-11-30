link:
	https://gist.github.com/bellbind/14dda289c7e35b2cec36273fd0f7c545
	created by bellbind, 2017

demo (original):
    offer side:		https://gist.githack.com/bellbind/14dda289c7e35b2cec36273fd0f7c545/raw/offer.html
    answer side:	https://gist.githack.com/bellbind/14dda289c7e35b2cec36273fd0f7c545/raw/answer.html

demo (within application container):
	offer side:		https://tabernakel:8001/internal/WEB-INF/webrtc-demo/offer.html
	answer side:	https://tabernakel:8001/internal/WEB-INF/webrtc-demo/answer.html

howto:
    open both offer side and answer side pages with recent firefox or chrome
    [offer side] "make" offer and wait a minute
    [offer side] copy the SDP offer (with "copy" button)
    [answer side] paste the SDP offer then "accept" (wait a minute when different browsers used)
    [answer side] copy the SDP answer data (with "copy" button)
    [offer side] paste the SDP answer then "accept"
    [both sides] "send" messages
    [both sides] "close" the connection

NOTE: the demo cannot connect offer/answer within same firefox(55.0.2) window.
