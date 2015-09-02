var React = require('react');

var PatientView = React.createClass({
	componentDidMount: function(){
		$('#patient-table').dataTable({paging: true, searching: false, info:true});
	},
	componentDidUpdate: function(){
		$('#patient-table').dataTable({paging: true, searching: false, info: true});
	},
	render: function() {

		var self = this;

		var resultArray = this.props.items.results;

		var resultItems = (
				resultArray.map(function(item, index){
		      		return (
				    	     <tr className="resultRow" style={{"cursor" : "pointer"}} onclick="" onClick={self.onPatientClick.bind(this, item.id, index)}>
				    	     	<td> {item.id}</td>
				    	     	<td> {item.name}</td>
				    	     	<td> {item.gender}</td>
				    	     	<td> {item.nStudies}</td>
				    	     </tr>
			           	);
       			})
			);

	return (
			<div>
				<table id="patient-table" className="table table-striped table-bordered" cellspacing="0" width="100%">
					<thead>
           				<tr>
                			<th>Id</th>
                			<th>Name</th>
                			<th>Gender</th>
                			<th>Studies</th>
            			</tr>
        			</thead>
        			 <tbody>
           				{resultItems}
            		</tbody>
    			</table>
			</div>
		);
	},

	onPatientClick:function(id, index){
		console.log("Patient clicked");
		this.props.onItemClick(this.props.items.results[index]);
	}

});

export {PatientView};
