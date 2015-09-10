var React = require('react');
var ReactBootstrap = require('react-bootstrap');
var Button = ReactBootstrap.Button;
var ModalTrigger = ReactBootstrap.ModalTrigger;
var Modal = ReactBootstrap.Modal;

import {SearchStore} from '../../../stores/searchStore';

import {ActionCreators} from '../../../actions/searchActions';
import {unindex} from '../../../handlers/requestHandler';
import {ConfirmModal} from './confirmModal';

import {Endpoints} from '../../../constants/endpoints';

var ImageView = React.createClass({
  	getInitialState: function() {
    	return {data: [],
    	status: "loading", 
      enableAdvancedSearch: this.props.enableAdvancedSearch};
  	},
    componentDidMount: function(){
       var self = this;

       $('#imagetable').dataTable({paging: true,searching: false,info:true});

     },
     componentDidUpdate: function(){

     },
    componentWillMount: function() {
      // Subscribe to the store.
      SearchStore.listen(this._onChange);
    },
	render: function() {
		var self = this;

		var resultArray = this.props.serie.images;

		var resultItems = (
				resultArray.map(function(item){
              
              var advOpt = (self.state.enableAdvancedSearch) ? (<td> 
                      <ModalTrigger modal={<ConfirmModal onConfirm={self.onUnindexClick.bind(null, item)}/>}>
                        <button className="btn btn_dicoogle btn-xs fa fa-eraser"> Unindex</button>
                      </ModalTrigger>
                      <ModalTrigger modal={<ConfirmModal onConfirm={self.onRemoveClick.bind(null, item)} message="The following files will be unindexed and then deleted from their storage"/>}>
                    <button className="btn btn_dicoogle btn-xs fa fa-trash-o"> Remove</button>
                  </ModalTrigger>
                    </td>) : undefined;

		      		return (
				    	     <tr>
				    	     	<td> {item.filename}</td>
				    	     	<td> {item.sopInstanceUID}</td>
				    	     	<td> NA</td>
				    	     	<td>
				    	     		<ModalTrigger modal={<PopOverView uid={item.sopInstanceUID}/>}>
    									 <button type="button" className="btn btn_dicoogle">Dump Image</button>
  									  </ModalTrigger>
                      <ModalTrigger modal={<PopOverImageViewer uid={item.sopInstanceUID}/>}>
                        <button type="button" className="btn btn_dicoogle">Show Image</button>
                      </ModalTrigger>
  								  </td>
                    {advOpt}
				    	     </tr>)
                    })
       			
			);
    var header = (self.state.enableAdvancedSearch) ? (
      <tr>
            <th>FileName</th>
            <th>SopInstanceUID</th>
            <th>Thumbnail</th>
            <th></th>            
            <th>Options</th>          
          </tr>
      ) : (
      <tr>
            <th>FileName</th>
            <th>SopInstanceUID</th>
            <th>Thumbnail</th>
            <th></th>
          </tr>
      );

	return (
			<div>
				<table id="imagetable" className="table table-striped table-bordered" cellspacing="0" width="100%">
					<thead>
           				{header}
        			</thead>
        			 <tbody>
           				{resultItems}
            		</tbody>
    			</table>
			</div>
		);
	},
  onUnindexClick: function(item){
    console.log(item)
    var uris = []; 
    uris.push(item.uri);
    let p = this.props.provider;
    ActionCreators.unindex(uris, p);
  },
  onRemoveClick: function(item){
    var uris = []; 
    uris.push(item.uri);
    ActionCreators.remove(uris);
  },
    _onChange : function(data){
      console.log("onchange");
      console.log(data.success);
      console.log(data.status);
      if (this.isMounted())
      {
        this.setState({data: data.data,
        status:"stopped",
        success: data.success, 
        enableAdvancedSearch: data.data.advancedOptions});
      }
    }
});


import {DumpStore} from '../../../stores/dumpStore';
import {DumpActions} from '../../../actions/dumpActions';

var PopOverView = React.createClass({
	getInitialState: function() {
    	return {data: [],
    	status: "loading",
    	current: 0, enableAdvancedSearch: this.props.enableAdvancedSearch};
  	},
  	componentDidMount: function() {
  		console.log("BILO ",this.props.uid);
  		DumpActions.get(this.props.uid);
      //$("#image1").imgViewer();
  	},
  	componentWillMount: function() {
    	// Subscribe to the store.
    	DumpStore.listen(this._onChange);
  	},
    componentDidUpdate: function(){

     $('#dumptable').dataTable({paging: false,searching: false,info: false,
       responsive: false

       });

    },

  	_onChange: function(data){
  		if (this.isMounted())
	    {
	    	console.log("adgsdg",data);
	      this.setState({data:data,
	      status:"stopped"}
	      );
	    }
  	},

	render:function(){
		if(this.state.status == "loading")
			return (
				<Modal  {...this.props}bsStyle='primary' title='Image Dump' animation={true}>
				<div> loading... </div>
        </Modal>);

				var obj = this.state.data.data.results.fields;
				var rows = [];

        var fields = [];
				Object.keys(obj).forEach(function(key, i) {
        			rows.push(<p key={i}><b>{key}:</b> {obj[key]}</p>);
              fields.push({att: key, field: obj[key]});
   				 });

var fieldstable = fields.map(function(item){
  return (
    <tr>
      <td> <p>{item.att}</p></td>
      <td> <p>{item.field}</p></td>
      </tr>
  );
});



		return (
			<Modal  {...this.props} bsStyle='primary' title='Image Dump' animation={true}>
		        <div className='modal-body'>

              <table id="dumptable" className="table-test table table-striped table-bordered responsive" cellspacing="0" width="100%">
                <thead>
                         <tr>
                            <th>Attribute</th>
                            <th>Field</th>
                          </tr>
                    </thead>
                     <tbody>
                         {fieldstable}
                      </tbody>
                </table>
            </div>
		        <div className='modal-footer'>
		          <Button onClick={this.props.onRequestHide}>Close</Button>
		        </div>
			</Modal>

			);
	}
});

var PopOverImageViewer = React.createClass({
	getInitialState: function() {
    	return {data: [],
    	status: "loading",
    	current: 0};
  	},


	render:function(){
	  var url = Endpoints.base + "/dic2png?SOPInstanceUID="+this.props.uid;
		return (
			<Modal  {...this.props} bsStyle='primary' title='Image Dump' animation={true}>
		        <div className='modal-body'>

             <img  id="image1" src={url} width="100%" />
		        </div>
		        <div className='modal-footer'>
		          <Button onClick={this.props.onRequestHide}>Close</Button>
		        </div>
			</Modal>

			);
	}
});


export {ImageView};
