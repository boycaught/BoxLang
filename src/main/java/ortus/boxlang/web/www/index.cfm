<cfoutput>
	<img src="/images/BLLogo.png" align="right">

	<h1>My BoxLang website!</h1>

	<p>This page is being rendered BoxLang running inside of a super lightweight Undertow Server</p>
	<p>There is no servlet container, just pure Java web server goodness!</p>

	<hr>

	<b>now():</b> #now()#<br>  
	<b>url.jacob:</b> #url.jacob ?: "none"#<br>
	<b>cgi.SCRIPT_NAME:</b> #cgi.SCRIPT_NAME# <br>
	#cookie.asString()#
	<cfset cookie.lastHit = now()>
	<cfset dump( {
		foo : "bar", 
		baz : "bum", 
		"nested" : [
			"brad",
			"luis",
			[ 
				"jacob" , 
				35, 
				true 
			]
		] 
	} )>

	<!--- <cfset dump( new java:java.io.File("") )> --->
</cfoutput>
kōwhai
<cfscript>
	colours = queryNew("id,en,mi", "integer,varchar,varchar", [
    [1,"red","whero"],
    [2,"orange","karaka"],
    [3,"yellow","kōwhai"],
    [4,"green","kākāriki "],
    [5,"blue","kikorangi"],
    [6,"indigo","tūāuri"],
    [10,"violet","papura"]
])
writeDump(colours)
</cfscript>