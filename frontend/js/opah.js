


document.addEventListener
(
	"DOMContentLoaded", domLoaded
);





function domLoaded( ev )
{
	var form = document.forms["main"];
	prepareForm( form );
	prepareBackButton();
	form.addEventListener
	(
		"submit",
		function ( ev )
		{
			ev.preventDefault();
			makeRequest( this );
		}
	);
}

function prepareBackButton()
{
	var button_back = 
		document
			.querySelector("article.request-response")
			.querySelector("button");

	button_back.addEventListener
	(
		"click",
		function (ev)
		{
			this.previousElementSibling.innerHTML = "";
			document.body.classList.remove("switch-to-show-response");
		}
	);

}


function makeRequest( form )
{
	var elements = form.elements;
	var data = 
	{
		checkin:elements.checkin.value,
		checkout:elements.checkout.value,
		children_count:elements.children_count.value,
		citycode:elements.citycode.value,
		adults_count:elements.adults_count.value
	};

	loader( true );

	AjaxRequest
	(
		data,
		"http://localhost:8000/ask",
		function ( response )
		{
			console.log( response );

			draw( response );

			setTimeout
			(
				function (ev)
				{
					loader( false );
				},
				3000
			);
		}
	);
}


function draw( response )
{
	var container = 
		document
			.querySelector(".request-response")
			.querySelector(".container");

	document.body.classList.add("switch-to-show-response");

	try
	{
		var parsed = JSON.parse( response );
	}
	catch( exception )
	{
		console.log( exception );
		container.innerHTML = sprint("Could not reach App, $0", [ exception.toString() ]);
		return;
	}

	if ( parsed.success === false )
	{
		container.innerHTML = parsed.description;
		return;
	}

	for (var i = 0 ; i < parsed.result.length ; i++)
	{
		var each = parsed.result[i];
		var each_el = document.createElement("div");
		each_el.innerHTML = sprint("<div>$0, $1</div>", [ each.name, each.cityName ] );
		var raw = JSON.stringify( each );
		var raw_el = document.createElement("div");

		raw_el.innerHTML = raw;
		container.appendChild( each_el );
		var rooms = parsed.result[i].rooms;

		for ( var j = 0 ; j < rooms.length ; j++ )
		{
			var room = rooms[ j ];
			var room_el = document.createElement("div");

			room_el.innerHTML = 
				sprint
				(
					"RoomID: $0, $1, TotalPrice: <span>R$ $2</span>", 
					[ room.roomID, room.categoryName, room.totalPrice ]
				);

			each_el.appendChild( room_el );
		}

		each_el.appendChild( raw_el );
	}

}


function loader( on )
{
	if ( !!on === true )
	{
		document.body.classList.add("loading");
	}
	else
	{
		document.body.classList.remove("loading");
	}
}

function prepareForm( form )
{
	var elements = form.elements;

	for (var i = 0 ; i < elements.length - 1; i++)
	{
		var each = elements[ i ];
		each.required = "true";
		each.placeholder = each.nextElementSibling.innerHTML; 
	}
}
