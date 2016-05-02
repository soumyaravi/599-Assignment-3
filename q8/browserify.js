var width =960,
	height =600;
var fill = d3.scale.category20();

d3.csv("language.csv", function(data){
	console.log("in");
	var metadata = data
	.filter(function (d){ return +d.count>0 ; })
	.map(function(d){ return { text: d.language, size :10 + Math.random()*50};})
	.sort(function(a,b) { return d3.descending(a.size, b.size); })
	/*.slice(0,25)*/;
	
var layout = d3.layout.cloud()
    .size([width, height])
    .words([
      metadata].map(function(d) {
      return {text: d, size: 10 + Math.random() * 50};
    }))
    .padding(0)
    .rotate(function() { return (Math.random() * 2) * 90; })
    .font("Impact")
    .fontSize(function(d) { return d.size; })
    .on("end", draw(metadata));
layout.start();
});
function draw(words) {
  d3.select("body").append("svg")
      .attr("width", width)
      .attr("height", height)
    .append("g")
      .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
    .selectAll("text")
      .data(words)
    .enter().append("text")
      .style("font-size", function(d) { return d.size + "px"; })
      .style("font-family", "Impact")
      .style("fill", function(d, i) { return fill(i); })
      .attr("text-anchor", "middle")
      .attr("transform", function(d) {
        return "translate(" + [(Math.random() * 200)-100, (Math.random() * 200)-100] + ")rotate(" + (Math.random() * 2)*90 + ")";
      })
      .text(function(d) { return d.text; });
	console.log(words);
}
