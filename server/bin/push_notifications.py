from gcm import *
import sys
import argparse

parser = argparse.ArgumentParser(description='Send a message.')
parser.add_argument('title', metavar='T', type=str, nargs='?',
		                    help='the title of the message',
												default='Default Title')
parser.add_argument('body', metavar='M', type=str, nargs='?',
		                    help='the message to send', 
												default='Default Body')

args = vars(parser.parse_args() )
title = args['title']
body = args['body']

gcm = GCM("AIzaSyA02SxebkYb9TcLvKLc6fHC7QRU9UgbHpE")
gcm = GCM("AIzaSyA02SxebkYb9TcLvKLc6fHC7QRU9UgbHpE")
data = {'title': title, 'body': body}

topic="live_basketball"

response = gcm.send_topic_message(topic=topic, data=data)

print(response)
