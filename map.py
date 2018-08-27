# -*- coding: utf-8 -*-
import urllib2
import urllib
import json
from math import sin, cos, sqrt, atan2, radians
# url='https://map.naver.com/spirra/findCarRoute.nhn?route=route3&output=json&result=web3&coord_type=naver&search=2&car=0&mileage=12.4&start=129.0798453%2C35.2333798%2C부산대&destination=129.000925%2C35.203551%2C구포초등학교&via='
# naver navi. valid link-> http://map.naver.com/spirra/findCarRoute.nhn?route=route3&output=json&result=web3&coord_type=naver&search=2&car=0&mileage=12.4&start=129.0798453%2C35.2333798%2C%EB%B6%80%EC%82%B0%EB%8C%80&destination=129.000925%2C35.203551%2C%EA%B5%AC%ED%8F%AC%EC%B4%88%EB%93%B1%ED%95%99%EA%B5%90&via=%27
# naver navi. error link-> https://map.naver.com/spirra/findCarRoute.nhn?route=route3&output=json&result=web3&coord_type=naver&search=2&car=0&mileage=12.4&via=&start=129.079845%2C35.2333798&destination=126.5616235%2C33.457945

targeturl = "https://maps.googleapis.com/maps/api/geocode/json?"
url_head = 'https://map.naver.com/spirra/findCarRoute.nhn?route=route3&output=json&result=web3&coord_type=naver&search=2&car=0&mileage=12.4&'
user_agent = "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)"
accept_language = "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3"

def get_D(lat, lon, Dlat, Dlon):
    query = {'start': str(lon) + ',' + str(lat), 'destination': str(Dlon) + ',' + str(Dlat), 'via':''}
    data = urllib.urlencode(query)
    url = url_head+data
    req = urllib2.Request(url)
    req.add_header("User-agent", user_agent)
    response = urllib2.urlopen(req)
    headers = response.info().headers
    data = json.loads(response.read())
    if 'routes' in data:
        return data['routes'][0]['summary']['distance']
    # the case treats an island
    elif 'error' in data:
        return 400000
    else:
        R = 6373.0
        lat1 = radians(lat)
        lon1 = radians(lon)
        lat2 = radians(Dlat)
        lon2 = radians(Dlon)
        dlon = lon2 - lon1
        dlat = lat2 - lat1
        a = sin(dlat/2)**2 + cos(lat1)*cos(lat2)*sin(dlon/2)**2
        c = 2*atan2(sqrt(a), sqrt(1-a))
        return int(round(R * c * 1000))

def get_where(en_name):
    p = {'address' :en_name,'key':'AIzaSyC-dfPkXCqv477XSn1BXC_VriEwB2s1axQ'}
    q = urllib.urlencode(p)
    url = targeturl+q
    req = urllib2.Request(url)
    req.add_header("User-agent", user_agent)
    req.add_header("Accept-Language", accept_language)
    response = urllib2.urlopen(req)
    headers = response.info().headers
    data = json.loads(response.read())
    # if data['results'][0]['address_components'][0]['types'][0]=='premise':
    #     name=data['results'][0]['address_components'][1]['long_name']
    #     address=data['results'][0]['formatted_address']
    #     lat= data['results'][0]['geometry']['location']['lat']
    #     lng=data['results'][0]['geometry']['location']['lng']
    print(url)
    result = {}
    result['name'] = data['results'][0]['address_components'][1]['long_name']
    result['address'] = data['results'][0]['formatted_address']
    result['lat'] = data['results'][0]['geometry']['location']['lat']
    result['lon'] = data['results'][0]['geometry']['location']['lng']
    return result