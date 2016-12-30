// Delay loading any function until the html dom has loaded. All functions are
// defined in this top level function to ensure private scope.
jQuery(document).ready(function () {

    // Installs error handling.
    jQuery.ajaxSetup({
        error: function (resp, e) {
            if (resp.status == 0) {
                alert('You are offline!!\n Please Check Your Network.');
            } else if (resp.status == 404) {
                alert('Requested URL not found.');
            } else if (resp.status == 500) {
                alert('Internel Server Error:\n\t' + resp.responseText);
            } else if (e == 'parsererror') {
                alert('Error.\nParsing JSON Request failed.');
            } else if (e == 'timeout') {
                alert('Request timeout.');
            } else {
                alert('Unknown Error.\n' + resp.responseText);
            }
        }
    });

    jQuery(function () {
        // list services
        $.get("/fsm/monitoring/list", function (availableServices) {
            var svcSelect = $("#svcSelect");
            svcSelect.append(new Option("--"));
            $.each(availableServices, function (key, value) {
                svcSelect.append(new Option(value));
            });
        });

     /*   var availableServices = ['/vrbc/common/fsm/examples/phone'];
        var svcSelectEle = $("#svcSelect");
        svcSelect.append(new Option("--"));
        $.each(availableServices, function (key, value) {
            svcSelectEle.append(new Option(value));
        }); */
    });

    // Setup accordion

/*    $('#svcDocView h3').bind('click', function () {
        var self = this;
        setTimeout(function () {
            theOffset = $(self).offset();
            $('body,html').animate({scrollTop: theOffset.top - 100});
        }, 310); // ensure the collapse animation is done
    });*/

/*    $("#svcDocView").accordion({
        collapsible: true,
        heightStyle: "content",
        active: 0,
        animate: 300,
        beforeActivate: function (event, ui) {
            if (ui.newHeader) {
                console.log('UI newheader value is ' + ui.newHeader.text());
                if (ui.newHeader.text().length > 0) {
                    ui.newPanel.html("");
                    $.get("/fsm/monitoring/doc/graphson?uri=" + ui.newHeader.text(), function (gData) {
                        var svg = Viz(gData[ui.newHeader.text()], "svg");
                        console.log('json payload is ' + gData);
                        ui.newPanel.html("<hr>" + svg);
                    });
                }
            }
        }
    });*/

    //on service select, populate service docs aka accordion
    $("#svcSelect").change(function () {
        var text = $('#svcSelect').val();
        if (text.indexOf('/') > -1) {
            console.log('selected option is ' + text);
            //populate service view
            $('#svcView').html = "";
            $.get("/fsm/monitoring/svc/graphson?uri=" + text, function (gData) {
                var svg = Viz(gData[text], "svg");
                console.log('json payload is ' + gData);
                $('#svcView').html("<hr>" + svg);
            });
            //fetch all child documents and populate accordion
            var svcDocIdEle = $('#svcDocId');
			
            $.get(text, function (results) {
				if(results.documentCount > 0) {
					svcDocIdEle.empty();
					$('#svcDocViewPanel').css("visibility","visible");
					$.each(results.documentLinks, function (key, value) {
						var newDiv = '<li class="list-group-item svcDocIdCls" >' + value + '</li>';
						svcDocIdEle.append(newDiv);
					}); 
					var docGView = $('#svcDocGraphView');
					docGView.html("");
					$('.svcDocIdCls').click(function() { 
						 var id = $(this).html(); 
						 $.get("/fsm/monitoring/doc/graphson?uri=" + id, function (gData) {
							var svg = Viz(gData[id], "svg");
							console.log('json payload is ' + gData);
							docGView.html("<hr>" + svg);
						});
						 console.log(id);
					  });
				} else {
					alert('No service documents');
				}
                              
            });
			
			 $('.svcDocIdCls').click(function() { 
				 var id= $(this).attr("value"); // Get the ID
				 console.log(id);
			  });
            //fetch all child documents and populate accordion
          /*  var accordion = $('#svcDocView');
            $.get(text, function (results) {
                $.each(results.documentLinks, function (key, value) {
                    var newDiv = "<div><h5>" + value + "</h5><div></div></div>";
                    accordion.append(newDiv);
                });
                accordion.accordion("refresh");
            }); */
        }
    });
});

