kt = keyTemplate
kp = keyPattern

# say
say msg 

examples : 
say "hello"
say "hello {{u/name}}"

# ask
ask kt question
the kt contains the result as a string param

askInteger kt question

examples : 
ask {{u/name}} "what's your name ?"
ask {{u/name/age}} "How old are you ?"

#[Conditions] on Integers

xx kt value
with xx in  
- lt
- lte
- gt
- gte
- eq
- neq

# Basic operations on Integers

decr kt
incr kt
incrBy kt increment 
set kt value

# [Conditions] on Strings

xx kt value
with xx in  
- eq
- neq
- eqIgnoreCase
- startsWith
- endsWith
- contains

# Basic operations on Strings

append kt something
set kt something

# vars
copy kt1 kt2
isFilled kt
delete kp 
set kt value or something [cf. ]

examples : 
isFilled {{u/name}}

#Status
succeed [returns a condition]
fail [returns a condition]
running 

#Composites 

begin sequence 
	BTN+
end sequence
	
begin sequence 
	BTN+
end selector

begin try tries
	BTN+
end try

begin loop  loops
	BTN+
end loop




