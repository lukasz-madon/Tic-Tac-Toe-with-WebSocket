<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <title>Tac Tac Toe</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js" type="text/javascript"></script>
	    <script src="jquery.json-2.3.js" type="text/javascript"></script>
	    <script src="tictactoe.js" type="text/javascript"></script>	    
	    
	    <style type="text/css">
	        
	            div.grid {
	                float: left;
	                width:50px;
	                height:50px;
	                outline:1px solid #bbb;
	                padding-left: 10px;
	                padding-bottom: 10px;
	                background:white;
	                float:left;
	                font-size:50px;
	                overflow:hidden;
	                font-family: sans-serif;
	            }
				
                    div.centre
                    {
                            width: 500px;
                            display: block;
                            margin-left: auto;
                            margin-right: auto;
                    }
                    div.centreinside
                    {
                            width: 220px;
                            display: block;
                            margin-left: auto;
                            margin-right: auto;
                    }
	            
	            div:nth-of-type(3n + 1) {
	                clear:left
	            }
	                
	            p {
	                clear:both;
	                padding:10px 0;
	            }
	            
	            div.X {
	                color: green;
	            }
	        
	            div.O {
	                color: red;
	            }       
	        
	    </style>	    
	</head>

	<body>

	<div class="centre">
		<img src="img/logo.png"/><br/><br/>
		<div class="centreinside">
			<div class="grid" id="grid_1">&nbsp;</div>
			<div class="grid" id="grid_2">&nbsp;</div>
			<div class="grid" id="grid_3">&nbsp;</div>
			<div class="grid" id="grid_4">&nbsp;</div>
			<div class="grid" id="grid_5">&nbsp;</div>
			<div class="grid" id="grid_6">&nbsp;</div>
			<div class="grid" id="grid_7">&nbsp;</div>
			<div class="grid" id="grid_8">&nbsp;</div>
			<div class="grid" id="grid_9">&nbsp;</div>
		
			<p id="status">&nbsp;</p>
		</div>	
	</div>	
	</body>
</html>

