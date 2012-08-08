/******************************************************/
/*				BASIC FUNCTIONALITY					  */
/******************************************************/
$(document).ready(function() {
	
	initPostButtons();
	
}); 


function initPostButtons(){
	

	
	//Add event for expand and collapse post content
	$(".postMore .toggleButton").on("click", function() {
		$(this).closest(".post").find(".fullText").fadeToggle("400", "linear", function() {		
			$(this).closest(".post").find(".toggleButton").toggle();
		});
	});
}
