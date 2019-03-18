# SmartSimulator
Build status: [![build_status](https://travis-ci.org/lujian213/SmartSimulator.svg?branch=master)](https://travis-ci.org/lujian213/SmartSimulator)

*SmartSimulator* is a generic simulator framework which can be used to simulate the behavior of specific service. It can be used in development or QA stage to reduce dependency and improve productivity.

The simulator itself has no business logic, it can behave/send back response purely based on the rules you defined as scripts.

## 1. Scripts Structure
An instance of SmartSimulator can host many simulator scripts. All these simulator scripts are centralized in a folder and each simulator script is represented as a sub-folder in it.

A simulator script folder can consists of 3 types of files.
###	1.1 init.properties	
A properties file define multiple key-value pairs. Besides some common properties, different type of simulators may have different properties. The properties definition follows [apache commons-configuration](http://commons.apache.org/proper/commons-configuration).
###	1.2 *.sim file	
sim file defines the request/response pairs. You can treat these pairs as rules. SmartSimulator has no business logic, but uses these rules to behave/send back response. If the incoming request matches the request template, then the simulator will construct the response  based on the response template and send back. Below is a sample of sim file.

```
GET {$url}/hello/{$name} HTTP/1.1

HTTP/1.1 200 OK

Hello! $name

------------------------------------------------------------------

GET {$url}/hi/{$name} HTTP/1.1

HTTP/1.1 200 OK

Good evening! $name

------------------------------------------------------------------

GET {$url}/error HTTP/1.1

HTTP/1.1 301 Moved Permanently
Location: http://www.baidu.com

------------------------------------------------------------------

```

Different request/response pair is seperated by a dotted line
<p><code>
`------------------------------------------------------------------`
</code></p>

Each request/response pair can consist of 1 request template and multiple response template. Different response template is seperated by the line started with *HTTP/1/1*.


###	1.3 sub-folder	
Depends on different type of simulator, it can also contain init.properties and .sim files. Outer-level properties and sim rules will be inherited by inner-level. And inner-level properties and rules can overwrite the inherited properties and rules from outer-level.

## 2. Templates
Template (request template and response template) is the core part of sim file to define the rule. 
### 2.1 Request Template
Request template by default is a reverse template. Below are some key points of reverse template.
<p>
* It is lined based. 
* All variables are defined as {$*var_name*}, like {$name}.
* Array type variable is ended with *[]*, like {$a[]}.
* Variable can set min and max length, like {$a:3,4}.
* Variable will be assigned values if the incoming message matched template.
</p>

For example, String `GET https://www.abc.com/hello/world HTTP/1.1` matched the template `GET {$url}/hello/{$name} HTTP/1.1`. After the match, variable `url = https://www.abc.com`, and `name = world`.

Another example, String `We have {$name[]} and {$name[]} in the team` matched the template `We have Alice and Bruce in the team`. After the match, variable `name[0] = Alice`, and `name[1] = Bruce`.

Request template has 3 parts: topline, headers, and payload.

*Topline* is one line description about the request. It is protocol specific. For example, `GET {$url}/hello/{$name} HTTP/1.1` for http protocol.

*Headers* are the required headers in the incoming request. Like `key = value`. All the keys which start with "_" is treated as internal control headers. They will not be used to match with incoming request headers. Current supported internal headers are listed below.

| Key | Value | Comments
| ------ | ------ | ----- |
| _Body-Type | XPath | Payload will be XPath List |
| _Body-Type | JSonPath | Payload will be JSonPath List |

If *_Body-Type* is not set, by default, it will be a normal reverse template. 

*Payload* is the main part of the incoming request. Its content is determined by the *_Body-Type* header as mentioned above.
* XPath list

```
messageId: //abc:MessageId/text()
dealId[]: //abc:PostEvent//abc:DealID[@SchemeName="foo"]/text() 
```

* JSonPath list

```
author: $.store.book[?(@.price>20)].author
price[]: $.store.book[?(@.author=='J. R. R. Tolkien')].price 
```

### 2.2 Response Template

