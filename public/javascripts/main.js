/*
 * MySpotify javascript logic
 *
 * Copyright (c) 2014 Harald Eilert
 *
 * Keep javascript logic here
 *
 */
   var MAIN = MAIN || (function(){
    var _wsInterface;// private
    var _htmlTemplate;// private
       return {
           init : function(wsInterface, mTemplate) {
                _wsInterface = wsInterface; //Interface for websocket
                _mTemplate = mTemplate; //Mustache template
           },
           execute : function() {

            //*******************************************************************************************
            //*********************Handle gui spinner while making ajax calls*****************************
            //*******************************************************************************************
            var loading = function(){
                 var docHeight = $(document).height();

                 $("body").append("<div id='overlay'></div>");

                 $("#overlay").height(docHeight).css({
                    'opacity' : 0.4,
                    'position': 'absolute',
                    'top': 0,
                    'left': 0,
                    'background-color': 'black',
                    'width': '100%',
                    'z-index': 5000
                 });
                 $("#spinner").show();
              }

             var doneLoading = function(){
                $("#spinner").hide();
                $("#overlay").remove();
             }

           //*******************************************************************************************
           //************When socket is retrieving a message use amaran to show it**********************
           //*******************************************************************************************
               var executeAmaranNotification = function(event){
                  if(event.data != $('#typeAheadArtist').val()){
                  $.amaran({
                       content:{
                           bgcolor:'#8e44ad',
                           color:'#fff',
                           message:'Another user is searching on: ' + event.data
                       },
                           theme:'colorful',
                           position:'bottom left',
                           closeButton:true,
                           cssanimationIn: 'rubberBand',
                           cssanimationOut: 'bounceOutUp'

                       });
                  }
               }

           //*******************************************************************************************
           //************************************* Typeahead stuff *************************************
           //*******************************************************************************************
              var artists = new Bloodhound({
                   datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
                   queryTokenizer: Bloodhound.tokenizers.whitespace,
                   remote: {
                       url: 'searchArtists?query=%QUERY',
                       ajax: {
                           beforeSend: function(){ $("#spinner2").show(); },
                           complete: function(){  $("#spinner2").hide(); }
                       }
                   }
               });

               artists.initialize();

               $('.typeahead').typeahead(
                   null, {
                   name: 'artists',
                   displayKey: 'name',
                   source: artists.ttAdapter()
               }).on('typeahead:selected', function(event, data){
                   loading();
                   populateArtistData(data.id, data.name)
               });

           //*******************************************************************************************
           //******************************Websocket stuff**********************************************
           //*******************************************************************************************

           // get websocket class, firefox has a different way to get it
           var WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;
           var socket = new WS(_wsInterface);

           //When client receives data from server
           //execute Amaran notification
           socket.onmessage = executeAmaranNotification;

            //*******************************************************************************************
            //******************************* Populate mustache template ********************************
            //*******************************************************************************************
             var populateArtistData = function(id, artist){
                 var template;
                 //Load the Mustache template used below
                 $.get(
                    _mTemplate,
                     function(d){
                         template = d
                     }
                 )
                 //Make call to get artist data
                 $.getJSON( "artistInfo?artistId="+id, function(data) {
                        var html = Mustache.to_html(template, data);
                        $('#artistWrapper').html(html);
                        $('#typeAheadArtist').val(artist);
                        //Tell server which artist that has been searched on!
                        socket.send(artist);
                 }).fail(function() {
                         alert("Couldn't retrieve data for artist!");
                 }).always(function() {
                         doneLoading();
                 });
             }

              $(document).on('click', "a.artistLink", function() {
                 loading();
                 var data = $(this).data('key');
                 populateArtistData(data.id, data.name)
              });
           }
       };
   }());