import requests
from dict2xml import dict2xml

data = {'departmentCode': 'EE'}
xml_data = dict2xml(data)
headers = {'Content-Type': 'text/xml'}
res = requests.post('http://localhost:8080/XMLServlet/courses', data=xml_data, headers=headers)
print(f'Response status code is {res.status_code}' )
print(res.text)