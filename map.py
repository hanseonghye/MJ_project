# -*- coding: utf-8 -*- 
import urllib2
import urllib
import json
#url='https://map.naver.com/spirra/findCarRoute.nhn?route=route3&output=json&result=web3&coord_type=naver&search=2&car=0&mileage=12.4&start=129.0798453%2C35.2333798%2C부산대&destination=129.000925%2C35.203551%2C구포초등학교&via='

url_head='https://map.naver.com/spirra/findCarRoute.nhn?route=route3&output=json&result=web3&coord_type=naver&search=2&car=0&mileage=12.4&'
user_agent = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"


def get_D(lat,lon,Dlat,Dlon):
	query={'start':lon+','+lat,'destination':Dlon+','+Dlat,'via':''}
	data=urllib.urlencode(query)
	url=url_head+data
	#print url
	req=urllib2.Request(url)
	req.add_header("User-agent", user_agent)
	response=urllib2.urlopen(req)
	headers=response.info().headers
	data=json.loads(response.read())

	print data['routes'][0]['summary']['distance']

get_D('35.2333798','129.079845','35.203551','129.000925')