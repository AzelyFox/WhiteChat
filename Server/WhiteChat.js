var express = require('express');
var app = express();
var http = require('http');
var server = http.createServer(app);
var io = require('socket.io')(server);
var mysql = require('mysql');
const dotenv = require('dotenv');
require('dotenv').config({path:'WhiteChat.env'});
var port = process.env.MAINPORT;
var FCM = require('fcm-node');

var List = require('./Modules/ListFactory.js');

var db_config = {
	host: process.env.DB_HOST,
	user: process.env.DB_USER,
	port: process.env.DB_PORT,
	password : process.env.DB_PASSWORD,
	database : process.env.DB_NAME,
	multipleStatements: true
};

var dbConnection;

handleDbDisconnect();

var FCM_SERVER_KEY = 'AAAAhP_M5bM:APA91bHUld2ueq7x3k11vuCgR8nm1sprPv9IewODBz2Zr6dABfkkovA2-PiHAO-059OMBr8sEe_NoA4gSxzNB4JtvhcpeUwBQ3bsbNqI8DQyE1kJy1R-_uVf8M9WNxMnuGMZd9o8qyq3';
var SOCKET_SECRET_KEY = 'K8W2N5A4';

function user() {
	this.num = 0;
	this.id = '';
	this.name = '';
	this.key = '';
}

server.listen(port, function () {
	console.log('Main Server listen (port %d)', port);
});

var fcm = new FCM(FCM_SERVER_KEY);

io.on('connection', function (socket) {
	console.log('new client connected');
	socket.user = new user();
	
	socket.on('login', function(data) {
		var requestID = data.requestID;
		var requestPW = data.requestPW;
		var loginQuery = "SELECT * FROM User WHERE User.user_id = ? LIMIT 1;SELECT User.user_index, User.user_id, User.user_nickname, User.user_rank, User.user_thumbnail FROM User WHERE User.user_index = ANY (SELECT Social.social_to FROM Social WHERE Social.social_from = (SELECT User.user_index FROM User WHERE User.user_id = ?));SELECT * FROM Room WHERE Room.room_index = ANY (SELECT Participant.room_index FROM Participant WHERE Participant.user_index = (SELECT User.user_index FROM User WHERE User.user_id = ?));";
		dbConnection.query(loginQuery, [requestID, requestID, requestID], function(err, results, fields) {
			if (err) {
				console.log('ID #' + requestID + ' Login : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('login', {
					result: 4,
					error: err
				});
				return;
			}
			if (results[0][0] == null) {
				console.log('ID #' + requestID + ' Login : No User');
				socket.emit('login', {
					result: 1,
					error: ''
				});
				return;
			}
			if (results[0][0].user_banned > 0) {
				console.log('ID #' + requestID + ' Login : Banned User');
				socket.emit('login', {
					result: 2,
					error: ''
				});
				return;
			}
			if (results[0][0].user_password !== requestPW) {
				console.log('ID #' + requestID + ' Login : Password Error');
				socket.emit('login', {
					result: 3,
					error: 'PASSWORD ERROR'
				});
				return;
			}
			socket.user.num = results[0][0].user_index;
			socket.user.id = results[0][0].user_id;
			socket.user.name = results[0][0].user_nickname;
			socket.user.key = xorCrypt(SOCKET_SECRET_KEY, results[0][0].user_password);
			console.log('ID #' + requestID + ' Login : Success');
			socket.emit('login', {
				result: 0,
				num: results[0][0].user_index,
				id: results[0][0].user_id,
				rank: results[0][0].user_rank,
				nickname: results[0][0].user_nickname,
				thumbnail: results[0][0].user_thumbnail,
				cash: results[0][0].user_cash,
				created: results[0][0].user_created,
				error: '',
				key: socket.user.key,
				socials: results[1],
				rooms: results[2]
			});
		});
	});
	
	socket.on('register', function(data) {
		var requestID = data.requestID;
		var requestPW = data.requestPW;
		var requestName = data.requestName;
		var defaultThumbnail = "https://avatars1.githubusercontent.com/u/" + (25262300 + Math.floor(Math.random() * 1000));
		var registerQuery = "INSERT INTO User (user_id, user_password, user_nickname, user_thumbnail) VALUES (?, ?, ?, ?)";
		dbConnection.query(registerQuery, [requestID, requestPW, requestName, defaultThumbnail], function(err, results) {
			if (err) {
				if (err.code === 'ER_DUP_ENTRY') {
					console.log('ID #' + requestID + ' Register : ID Exists');
					socket.emit('register', {
						result: 1,
						error: ''
					});
					return;
				} else {
					console.log('ID #' + requestID + ' Register : Unknown Error');
					console.log('Error > ' + err);
					socket.emit('register', {
						result: 2,
						error: err
					});
					return;
				}
			}
			console.log('ID #' + requestID + ' Register : Success');
			socket.emit('register', {
				result: 0,
				error: ''
			});
		});
	});
	
	socket.on('withdraw', function(data) {
		var confirmPW = data.confirmPW;
		var key = data.key;
		var withdrawQuery = "DELETE FROM User WHERE User.user_index = ? AND User.user_password = ?";
		if (socket.user == null || socket.user.num == null || socket.user.num == 0) {
			console.log('ID #' + socket.user.id + ' Withdraw : Not Login');
			socket.emit('withdraw', {
				result: 1,
				message: 'No Login Data',
				error: ''
			});
			return;
		}
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Withdraw : Key Incorrect');
			socket.emit('withdraw', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		dbConnection.query(withdrawQuery, [socket.user.num, confirmPW], function(err, results) {
			if (err) { // CONTAINS PASSWORD NOT MATCH CASE
				console.log('ID #' + socket.user.id + ' Withdraw : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('withdraw', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' Withdraw : Success');
			socket.emit('withdraw', {
				result: 0,
				message: '',
				error: ''
			});
		});
	});
	
	socket.on('refresh', function(data) {
		var key = data.key;
		var refreshQuery = "SELECT User.user_index, User.user_id, User.user_nickname, User.user_rank, User.user_thumbnail FROM User WHERE User.user_index = ANY (SELECT Social.social_to FROM Social WHERE Social.social_from = (SELECT User.user_index FROM User WHERE User.user_id = ?));SELECT * FROM Room WHERE Room.room_key = ANY (SELECT Participant.room_key FROM Participant WHERE Participant.user_index = (SELECT User.user_index FROM User WHERE User.user_id = ?));";
		dbConnection.query(refreshQuery, [socket.user.id, socket.user.id, socket.user.id], function(err, results, fields) {
			if (err) {
				console.log('ID #' + socket.user.id + ' Refresh : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('login', {
					result: 2,
					error: err
				});
				return;
			}
			console.log('ID #' + requestID + ' Refresh : Success');
			socket.emit('refresh', {
				result: 0,
				error: '',
				socials: results[0],
				rooms: results[1]
			});
		});
	});
	
	socket.on('relation', function(data) {
		var targetNUM = data.targetNUM;
		var relation = data.relation;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Relation : Key Incorrect');
			socket.emit('relation', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		if (relation == 1) {
			var relationQuery = "INSERT INTO Social (social_from, social_to, social_type) VALUES (?, ?, ?)";
			dbConnection.query(relationQuery, [socket.user.num, targetNUM, relation], function(err, results) {
			if (err) {
				console.log('ID #' + socket.user.id + ' Relation : friend : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('relation', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' Relation : friend : Success');
			socket.emit('relation', {
				result: 0,
				message: '',
				target: targetNUM,
				relation: relation,
				error: ''
			});
			});
		} else {
			var relationQuery = "DELETE FROM Social WHERE social_from = ? AND social_to = ?";
			dbConnection.query(relationQuery, [socket.user.num, targetNUM], function(err, results) {
			if (err) {
				console.log('ID #' + socket.user.id + ' Relation : block : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('relation', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' Relation : block : Success');
			socket.emit('relation', {
				result: 0,
				message: '',
				target: targetNUM,
				relation: relation,
				error: ''
			});
			});
		}	
	});
	
	socket.on('create', function(data) {
		var roomName = data.title;
		var roomPassword = data.title;
		var baseTarget = data.target;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Create : Key Incorrect');
			socket.emit('create', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		var createQuery = "INSERT INTO Room (room_name, room_password) VALUES (?, ?);";
		if (baseTarget) {
			var additionQuery = "INSERT INTO Participant (room_index, user_index) VALUES ((SELECT MAX(Room.room_index) FROM Room), " + socket.user.num + ");INSERT INTO Participant (room_index, user_index) VALUES ((SELECT MAX(Room.room_index) FROM Room), " + baseTarget + ");";
		} else {
			var additionQuery = "INSERT INTO Participant (room_index, user_index) VALUES ((SELECT MAX(Room.room_index) FROM Room), " + socket.user.num + ");";
		}
		createQuery = createQuery.concat(additionQuery);
		dbConnection.query(createQuery, [roomName, roomPassword], function(err, results) {
			if (err) {
				console.log('ID #' + socket.user.id + ' Create : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('create', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' Create : Success');
			socket.emit('create', {
				result: 0,
				message: '',
				error: ''
			});
		});
	});
	
	socket.on('join', function(data) {
		var room = data.room;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Join : Key Incorrect');
			socket.emit('join', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		var joinQuery = "INSERT INTO Participant (room_index, user_index) VALUES (?, ?)";
		dbConnection.query(joinQuery, [room, socket.user.num], function(err, results, fields) {
			if (err) {
				console.log('ID #' + socket.user.id + ' Join : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('join', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' Join : Success');
			socket.emit('join', {
				result: 0,
				message: '',
				error: ''
			});
		});
	});
	
	socket.on('leave', function(data) {
		var room = data.room;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Leave : Key Incorrect');
			socket.emit('leave', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		var leaveQuery = "DELETE FROM Participant WHERE Participant.user_index = ? AND Participant.room_key = ?";
		dbConnection.query(leaveQuery, [socket.user.num, room], function(err, results) {
			if (err) {
				console.log('ID #' + socket.user.id + ' Leave : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('leave', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' Leave : Success');
			socket.emit('leave', {
				result: 0,
				message: '',
				error: ''
			});
		});
	});
	
	socket.on('send', function(data) {
		var room = data.room;
		var content = data.message;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Send : Key Incorrect');
			socket.emit('send', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		var sendQuery = "INSERT INTO Message (message_room, message_owner, message_content) VALUES (?, ?, ?);SELECT NOW();";
		dbConnection.query(sendQuery, [room, socket.user.num, content], function(err, results) {
			if (err) {
				console.log('ID #' + socket.user.id + ' Send : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('send', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			var message = {
				message_index: results[0].insertId,
				message_room: room,
				message_owner: socket.user.num,
				message_content: content,
				message_created: results[1]
			};
			console.log('ID #' + socket.user.id + ' Send : Success');
			socket.emit('send', {
				result: 0,
				message: '',
				error: ''
			});
			io.in(room).emit('receive', {
				result: 0,
				room: room,
				message: message,
				error: ''
			});
		});
	});
	
	socket.on('retrieve_basics', function(data) {
		var room = data.room;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Send : Key Incorrect');
			socket.emit('send', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		var retrieveQuery = "SELECT * FROM Message WHERE message_room = ? ORDER BY message_index DESC LIMIT 1;SELECT COUNT(Participant.user_index) FROM Participant WHERE Participant.room_index = ?;";
		dbConnection.query(retrieveQuery, [room, room], function(err, results) {
			if (err) {
				console.log('ID #' + socket.user.id + ' RetrieveBasics : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('retrieve_basics', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' RetrieveBasics : Success');
			socket.emit('retrieve_basics', {
				result: 0,
				message: '',
				error: '',
				room: room,
				last_message: results[0],
				participant_count: results[1]
			});
		});
	});
	
	socket.on('retrieve', function(data) {
		var room = data.room;
		//var index = data.index;
		var index = -1;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Send : Key Incorrect');
			socket.emit('send', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		var retrieveQuery = "SELECT * FROM Message WHERE message_room = ? AND message_index > ?;SELECT User.user_index, User.user_nickname, User.user_rank, User.user_id, User.user_thumbnail FROM User WHERE User.user_index = ANY (SELECT Participant.user_index FROM Participant WHERE Participant.room_index = ?);";
		dbConnection.query(retrieveQuery, [room, index, room], function(err, results) {
			if (err) {
				console.log('ID #' + socket.user.id + ' Retrieve : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('retrieve', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' Retrieve : Success');
			socket.emit('retrieve', {
				result: 0,
				message: '',
				error: '',
				room: room,
				messages: results[0],
				participants: results[1]
			});
		});
	});
	
	socket.on('enter', function(data) {
		var room = data.room;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Enter : Key Incorrect');
			socket.emit('enter', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		socket.join(room);
	});
	
	socket.on('exit', function(data) {
		var room = data.room;
		var key = data.key;
		/*
		if (socket.user.key !== key) {
			console.log('ID #' + socket.user.id + ' Exit : Key Incorrect');
			socket.emit('exit', {
				result: 1,
				message: 'Key Incorrect',
				error: ''
			});
			return;
		}
		*/
		socket.leave(room);
	});
	
	socket.on('notices', function(data) {
		var start = data.start;
		var key = data.key;
	});
	
	socket.on('support', function(data) {
		var title = data.title;
		var content = data.content;
		var key = data.key;
	});
	
	socket.on('clientKey', function(data) {
		var key = data;
		if (!socket.user.num) {
			console.log('ID # NULL ClientKey : No Socket User');
			return;
		}
		var keyQuery = "UPDATE User SET user_client = ? WHERE user_index = ?";
		dbConnection.query(keyQuery, [key, socket.user.num], function(err, results) {
			if (err) {
				console.log('ID #' + socket.user.id + ' ClientKey : Unknown Error');
				console.log('Error > ' + err);
				socket.emit('clientKey', {
					result: 2,
					message: '',
					error: err
				});
				return;
			}
			console.log('ID #' + socket.user.id + ' ClientKey : Success');
			socket.emit('clientKey', {
				result: 0,
				message: '',
				error: ''
			});
		});
	});
});

function xorCrypt(key, str) {
	var output = '';
	for (var i = 0; i < str.length; ++i) {
      output += String.fromCharCode(key.charCodeAt((i%key.length) ^ str.charCodeAt(i)));
    }
	return output;
}

function handleDbDisconnect() {
	dbConnection = mysql.createConnection(db_config);

	dbConnection.connect(function(err) {
		if(err) {
			console.log('DB Connection Error > ' + err);
			setTimeout(handleDbDisconnect, 500);
		}
	});
	
	dbConnection.on('error', function(err) {
		console.log('DB Error > ' + err);
		if(err.code === 'PROTOCOL_CONNECTION_LOST') {
			console.log('DB Connection Lost');
			console.log('DB Retrying');
			handleDbDisconnect();
		} else {
			throw err;
		}
	});
}