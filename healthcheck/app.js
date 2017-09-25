const fetch = require('node-fetch');
const nodemailer = require('nodemailer');
const async = require('async');
const _ = require('lodash');
const spawn = require('child_process').spawn;
const moment = require('moment');

require('dotenv').config();

const uri = 'http://ec2-35-167-51-118.us-west-2.compute.amazonaws.com';

const auth = {
	user: process.env.SENDGRID_USER,
	pass: process.env.SENDGRID_PASSWORD,
};

const smtpTransport = nodemailer.createTransport({
	service: 'SendGrid',
	auth: auth,
});

runScript = (scriptPath, subject, message) => {
	async.waterfall([
		(done) => {
			try{
				const p = spawn('bash',[scriptPath]);
				let output = '';
				p.stdout.on('data', function(data) {
					output += data;
				});
				p.on('error', function(err) {
					done(null, null);
				});
				p.on('close' , function(code){
					done(null, output);
				});
			} catch (err) {
				done(null, null);
			}
		},
		(output, done) => {

			const mailOptions = {
				to: 'michaelsenpatrick@gmail.com',
				from: 'servercheck@livebasketball.com',
				subject: subject,
				html: message,
				attachments: [
					{
						filename: 'log.txt',
						content: new Buffer(output, 'utf-8'),
					}
				]
			};

			smtpTransport.sendMail( mailOptions, (err)=>{
				done(err,'done');
			});
		}
	]);
};

sendEmail = (subject, message) => {

	const mailOptions = {
		to: 'michaelsenpatrick@gmail.com',
		from: 'servercheck@livebasketball.com',
		subject: subject,
		html: message,
	};

	smtpTransport.sendMail( mailOptions, (err)=>{
		console.log('err', err);
	});
};

fetchData = () => {
	return fetch(uri + '/livebasketball/leagues', {
		method: 'GET',
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json',
		},
	});
};

console.log("App started");

let serverUp = true;
let serverInSync = true;

healthCheck = () => {
	console.log(`Doing health check. Server status is ${(serverUp)? 'ok':'down'}`);
	console.log(`Doing health check. Server is ${(serverInSync)? 'in sync':'out of sync'}`);
	fetchData()
		.then( res => {
			if(res.ok){
				if(!serverUp){
					sendEmail('Server Rest Back Online', `
						You are receving this email because the server
						rest service stopped and was successfully restarted
						automatically.
					`);
				}
				serverUp = true;
				return res.json();
			}
			else{
				if(serverUp){
					runScript('print_logs.sh', 'Server Rest Service Stopped', `
						You are receiving this message because the server rest
						service has stopped: 
						Server status was ${res.status}
					`);
					runScript('restart_rest.sh', 'Server Rest Restart Attempted', `
						You are receiving this message because the rest
						has attempted an automatic restart. See the attached
						output
					`);
				}
				serverUp = false;
			}
		})
		.then( data => {
			if(data && data.leagues){
				const timestamp = _.find(data.leagues, (value, key) => {
					return (value.id || '').indexOf('#') > -1;
				});
				const matches = timestamp.id.match(/[0-9]:[0-9][0-9]/);
				const serverMinutes = moment(matches[0],'h:mm').minutes();
				const localMinutes = moment().minutes();
				if( localMinutes - 2 < serverMinutes && serverMinutes < localMinutes + 2){
					if(!serverInSync){
						sendEmail('Server Scraper Back Online', `
							You are receiving this email because the server
							scraper stopped and was successfully restarted
							automatically.
						`);
					}
					serverInSync = true;
				}
				else {
					if(serverInSync){
						runScript('print_logs.sh', 'Server Scraper Stopped', ` 
							You are receiving this message because the server 
							scraper service has stopped: Scraper is out of sync.
						`);
						runScript('restart_scraper.sh', 'Server Scraper Restart Attempted', `
							You are receiving this message because the scraper
							has attempted an automatic restart. See the attached
							output
						`);
					}
					serverInSync = false;
				}
			}
		})
		.catch( err => {
			console.log(err);
		});
};

const healthCheckInterval = setInterval(healthCheck, 5000); 
