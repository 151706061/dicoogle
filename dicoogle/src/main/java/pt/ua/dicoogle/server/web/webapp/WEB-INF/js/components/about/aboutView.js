var React = require('react');


require('bootstrap');
var React = require('react');
var Router = require('react-router');
var Flux = require('flux');
var ReactBootstrap = require('react-bootstrap');

var Route = Router.Route;
var DefaultRoute = Router.DefaultRoute;
var Routes = Router.Routes;

var Link = Router.Link;
var NotFoundRoute = Router.NotFoundRoute;
var  RouteHandler = Router.RouteHandler;

var Nav = ReactBootstrap.Nav;

var NavItemLink = ReactBootstrap.NavItemLink;
var ButtonLink = ReactBootstrap.ButtonLink;

var Grid = ReactBootstrap.Grid;
var Row = ReactBootstrap.Row;
var Col = ReactBootstrap.Col;

var Panel = ReactBootstrap.Panel;
var Navbar = ReactBootstrap.Navbar;
var NavItem = ReactBootstrap.NavItem;
var DropdownButton = ReactBootstrap.DropdownButton;
var MenuItem = ReactBootstrap.MenuItem;
var  RouteHandler = Router.RouteHandler;
var ButtonToolbar = ReactBootstrap.ButtonToolbar;
var Button= ReactBootstrap.Button;
var ProgressBar= ReactBootstrap.ProgressBar;
var Table= ReactBootstrap.Table;


var AboutView = React.createClass({
    componentDidMount: function() {


    },
      render: function() {
          var title = (
              <h3>Dicoogle PACS</h3>
          );
          var divStyle = {
              width: '200px'
          };
          var licenses = (
              <Grid className="">

          <Row className="show-grid">
          <Col className="gridAbout" xs={2} md={2}><b>dcm4che2</b><br/>License: GPL</Col>
          <Col className="gridAbout" xs={2} md={2}><b>react.js+flux</b><br/>License: GPL</Col>
          <Col className="gridAbout" xs={2} md={2}><b>Jetty</b><br/>License: GPL</Col>
          </Row>


          </Grid>
          );


          var panelsInstance = (
              <div className="about">
          <Panel header={title} bsStyle="primary">
          Dicoogle is an open source medical imaging repository using Indexing System and Distributed mechanisms. 
          Our solution can be used as PACS archive or can read your PACS archive file system and
          allow you to do PACS mining. Morever, it can be used to create your own extensions.
          Until now, we already indexed around 22 million of DICOM images and this
          number tends to increase. 
          There are several researchers working to evaluate and improve the quality of medical records and Dicoogle contributed to several case studies

              <br />

          </Panel>

          <Panel header="Main third party components" bsStyle="primary">
              
          {licenses}
              
           Note: not only this components are used, but these are the main components used.
          </Panel>

          <Panel header="Disclaimer" bsStyle="primary">
          This software is provided by the copyright holders and contributors "as is" and any express or
          implied warranties, including, but not limited to, the implied warranties of merchantability and
          fitness for a particular purpose are disclaimed. In no event shall the copyright owner or contributors
          be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
          (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption)
          however caused and on any theory of liability, whether in contract, strict liability,
              or tort (including negligence or otherwise) arising in any way out of the use of this software,
              even if advised of the possibility of such damage.
          </Panel>
          <Panel header="Developers" bsStyle="primary">
          As an open source software, Dicoogle can accept contributions from developers around the world.
          The lead team of Dicoogle OSS is Bioinformatics UA and BMD Software (and supported as well). More information check it  <a target="_new" href="http://www.dicoogle.com">in the Dicoogle website.</a>
          <div style={{display: 'inline-block', width: '100%'}}>
            <a href="http://bioinformatics.ua.pt"><img src="assets/logos/logobio.png" style={{height: 40, margin:5}} /></a>
            <a href="http://bmd-software.com/"><img src="assets/logos/logo.png" style={{height: 40, padding: 5, margin:5}} /></a>
            <a href="http://www.ieeta.pt/"><img src="assets/logos/logo-ieeta.png" style={{height: 60, margin:5}} /></a>
            <a href="http://www.ua.pt/"><img src="assets/logos/logo-ua.png" style={{height: 60, margin:5}} /></a>
        </div>
        <div style={{display: 'inline-block'}}>
            <a><img src="assets/logos/logoFCT.png" style={{height: 30, margin:5}} /></a>
        </div>
          </Panel>

          </div>
          );
          return panelsInstance;
          
          
        }
      });

    export {
      AboutView
    }
