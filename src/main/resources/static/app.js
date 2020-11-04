var stompClient = null;
var count = 1;
//var filter = '{"followers":["elon musk"],"keywords":["tesla"]}';
var filter={followers:[],
			keywords:[]};

var Count=0;
function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
}

function addFollowers(){
	if(document.getElementById("followers").value.length!=0){
		var b=document.createElement('button');
		b.setAttribute('class','btn btn -info btn-sm');
		b.setAttribute('id',"'"+Count+"'");
		b.setAttribute('onclick','remove(this)');
		b.innerHTML+="<span class='glyphicon glyphicon-remove'></span><div>"+document.getElementById("followers").value+"</div>";
		var wrapper=document.getElementById("followers-area");
		wrapper.appendChild(b);
		document.getElementById("followers").value="";
	}
}

function addKeywords(){
	if(document.getElementById("keywords").value.length!=0){
		var b=document.createElement('button');
		b.setAttribute('class','btn btn -info btn-sm');
		b.setAttribute('id',"'"+Count+"'");
		b.setAttribute('onclick','remove(this)');
		b.innerHTML+="<span class='glyphicon glyphicon-remove'></span><div>"+document.getElementById("keywords").value+"</div>";
		var wrapper=document.getElementById("keywords-area");
		wrapper.appendChild(b);
		document.getElementById("keywords").value="";
		
	}
}

function remove(e){
	e.parentElement.removeChild(e);
	
}

function enterKeyInFollowers(event){
	var key_board_keycode = event.which || event.keyCode;
    if(key_board_keycode == 13)
    {
       	addFollowers();
    }
}

function enterKeyInKeywords(event){
	var key_board_keycode = event.which || event.keyCode;
    if(key_board_keycode == 13)
    {
       	addKeywords();
    }
}

function submitFilters(){
	filter={followers:[],
			keywords:[]};
	var followersArea=document.getElementById("followers-area").children;
	for(var i=0;i<followersArea.length;i++){
		filter.followers[i]=followersArea[i].children[1].innerHTML;
	}
	
	var keywordsArea=document.getElementById("keywords-area").children;	
	for(var i=0;i<keywordsArea.length;i++){
		filter.keywords[i]=keywordsArea[i].children[1].innerHTML;
	}
	sendFilter();
	console.log(followersArea.length);
	console.log(filter);
		
}

function connect() {
	if(stompClient == null){
		httpGet('http://localhost:8080/connect');
	}
	var socket = new SockJS('/twitterSpringBoot');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {
		setConnected(true);
		sendFilter();

		stompClient.subscribe('/twitterspringboot/tweets', function(message) {
			if (JSON.parse(message.body).messege.localeCompare("SUCCESS") != 0)
				showTweets(JSON.parse(message.body).messege);
		});
	});
}

function disconnect() {
	if (stompClient !== null) {
		stompClient.disconnect();
		setConnected(false);
	}
}

function httpGet(url) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.open('GET', url, true);
	xmlHttp.send(null);
	return xmlHttp.responseText;
}

function sendFilter() {
	
	if(stompClient!=null)
	stompClient.send("/app/filterrules", {}, JSON.stringify(filter));
}

function showTweets(tweet) {

	var inner = document.getElementById('tweet-container');
	count++;
	if (count > 20) {
		inner.removeChild(inner.lastChild);
		count--;
	}
	
		inner.insertAdjacentHTML('afterbegin',"<div class='panel-body'>" + tweet + "</div>");
}