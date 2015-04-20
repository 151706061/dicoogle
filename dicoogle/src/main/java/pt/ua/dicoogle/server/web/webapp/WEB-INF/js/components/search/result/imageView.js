var React = require('react');
var ReactBootstrap = require('react-bootstrap');
var Button = ReactBootstrap.Button;
var ModalTrigger = ReactBootstrap.ModalTrigger;
var Modal = ReactBootstrap.Modal;

import {Endpoints} from '../../../constants/endpoints';

var ImageView = React.createClass({
  	getInitialState: function() {
    	return {data: [],
    	status: "loading"};
  	},
    componentDidMount: function(){
       var self = this;

       $('#imagetable').dataTable({paging: false,searching: false,info:false});

     },
     componentDidUpdate: function(){

      $('#imagetable').dataTable({paging: false,searching: false,info: false});

     },
	render: function() {
		var self = this;

		var resultArray = this.props.serie.images;

		var resultItems = (
				resultArray.map(function(item){
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
				    	     </tr>
			           	);
       			})
			);

	return (
			<div>
				<table id="imagetable" className="table table-striped table-bordered" cellspacing="0" width="100%">
					<thead>
           				<tr>
                			<th>FileName</th>
                			<th>SopInstanceUID</th>
                			<th>Thumbnail</th>
                      <th></th>
                    </tr>
        			</thead>
        			 <tbody>
           				{resultItems}
            		</tbody>
    			</table>
			</div>
		);
	},


});


import {DumpStore} from '../../../stores/dumpStore';
import {DumpActions} from '../../../actions/dumpActions';

var PopOverView = React.createClass({
	getInitialState: function() {
    	return {data: [],
    	status: "loading",
    	current: 0};
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
				<Modal  {...this.props}bsStyle='primary' title='Image Dump' animation={false}>
				<div> loading... </div>
        </Modal>);

				var obj = this.state.data.data.results.fields;
				var rows = [];
				Object.keys(obj).forEach(function(key, i) {
        			rows.push(<p key={i}><b>{key}:</b> {obj[key]}</p>);
   				 });


		return (
			<Modal  {...this.props} bsStyle='primary' title='Image Dump' animation={true}>
		        <div className='modal-body'>

		         {rows}
             <img  id="image1" src="http://globe-views.com/dcim/dreams/bananas/bananas-03.jpg" width="50%" />
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
