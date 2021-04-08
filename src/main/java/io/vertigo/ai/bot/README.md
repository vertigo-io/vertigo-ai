# conventions
k = key 			ex {{u/name}}
kt = keyTemplate  	ex {{u/name}} or {{u/name/{{idx}}}}
kp = keyPattern
BTN = BehaviorTreeNode  //returns succeeded or failed or running
BTC = BehaviorTreeCondition //returns succeeded or failed

# say
say msg 

examples : 
say "hello"
say "hello {{u/name}}"

# ask
ask kt question
the kt contains the result as a string param

ask:integer kt question

examples : 
ask {{u/name}} "what's your name ?"
ask {{u/name/age}} "How old are you ?"

#[Conditions] on Integers

xx kt value
with xx in  
- largerThan
- largerThanOrEqual
- greaterThan
- greaterThanOrEqual
- equals 
- notEquals

example 
greaterThan {{u/age}} 75

# Basic operations on Integers

decr kt
incr kt
add kt increment   //increment as int or kt
mult kt factor     //factor as int or kt

# [Conditions] on Strings

xx kt value
with xx in  
- equals
- notEquals
- equalsIgnoreCase
- startsWith
- endsWith
- contains

# Basic operations on Strings

append kt something

# Vars
copy kt1 kt2
isFilled kt
isEmpty kt
delete kp 
set kt value or something  // Integer or String

examples : 
isFilled {{u/name}}

# Status
succeed [returns a condition]
fail [returns a condition]
???? running 

# Composites 

begin sequence 
	BTN+
end sequence
	
begin selector 
	BTN+
end selector

begin try tries
	BTN+
end try

begin loop  loops
	BTN+
end loop


example :
context : {{color}} contains "A" "B" or "C"

begin selector 
	begin sequence 
		equals {{color}} "A"
		say "the color is blue"
	end sequence
	begin sequence
		equals {{color}} "B"
		say "the color is white"
	end sequence
	begin sequence
		equals {{color}} "C"
		say "the color is red"
	end sequence
	say "I don't know the chosen color ;-("
end selector

begin switch {{color}}
	case "A"
		say ...	
		say ..
	case "B"
		say...	
	case "C"
		say...	
	say...	
	say...	
	say...	
end switch

begin choose:button {{color}} "what is your ....color ?" 
	button "A" "bleu" 
	button "B" "blanc"
	button "C" "rouge"
end choose

???? How load a list of buttons (countries for example) 

# comments
-- this is a comment 
-- this is another line of comment

begin sequence -- a comment after a command
 
???? # while / until
begin while [ {{u/age}}>=75 or {{u/illness}}==1 ]
	((BTC and BTC) or BTC)  
	BTN+
end while 
	

# macros 
declare macro "administrator"  ???namespace 
	say "hello, you're an admin"
end macro

begin switch {{role}}
	case "C"
		macro "contributor" ???? namespace'	
	case "A"
		macro "administrator" 	
	case "U"
		macro "user" 	
end switch 





