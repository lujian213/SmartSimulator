# SmartSimulator
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
Request template is a reverse template. Below are some key points of it.
<p>
* It is lined based. 
* All variables are defines as {$*var_name*}, like {$name}.
* Array type variable is end with *[]*, like {$a[]}.
* Variable will be assigned values if the incoming message matched template.
</p>
<p>For example, String `GET https://www.abc.com/hello/world HTTP/1.1` matched the temaplte `GET {$url}/hello/{$name} HTTP/1.1`. After the match, variable `url = https://www.abc.com`, and `name = world`.</p>

<p>Another example, String `GET https://www.abc.com/hello/world HTTP/1.1` matched the temaplte `GET {$url}/hello/{$name} HTTP/1.1`. After the match, variable `url = https://www.abc.com`, and `name = world`.</p>
Request template has 3 parts: topline, headers, and payload.
<p>Topline is one line description about the request. It is protocol specific. For example, `GET {$url}/hello/{$name} HTTP/1.1` for http protocol.</p>

### 2.2 Response Template

