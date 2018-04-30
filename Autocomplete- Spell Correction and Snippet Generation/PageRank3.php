<?php 

include 'SpellCorrector.php';
include 'simple_html_dom.php';

// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');
ini_set('memory_limit',-1);
ini_set('max_execution_time', 300);
$correct_query="";
$limit = 10;
$flag = 0;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

if ($query)
{
  // The Apache Solr Client library should be on the include path
  // which is usually most easily accomplished by placing in the
  // same directory as this script ( . or current directory is a default
  // php include path entry in the php.ini)
  require_once('Apache/Solr/Service.php');

  // create a new solr service instance - host, port, and webapp
  // path (all defaults in this example)
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/ircore');
  $query_terms = explode(" ", $query);
  // if magic quotes is enabled then stripslashes will be needed
  if (get_magic_quotes_gpc() == 1)
  {
    $query = stripslashes($query);
  }
  for($i = 0 ; $i < sizeof($query_terms); $i++)
  {
  	$chk = SpellCorrector::correct($query_terms[$i]);
  	if($i == 0)
  		$correct_query = $correct_query . $chk;
  	else
  		$correct_query = $correct_query .' '. $chk;
  }
  if(strtolower($query) != strtolower($correct_query))
  {
  	$flag = 1;
  }
  // in production code you'll always want to use a try /catch for any
  // possible exceptions emitted  by searching (i.e. connection
  // problems or a query parsing error)
  try
  {
  	if($_GET['algo'] == "lucene")
  	{
  	/*	if($flag == 1)
  			$results = $solr->search($correct_query, 0, $limit);
    	else*/
    		$results = $solr->search($query, 0, $limit);
  	}
  	else
  	{
  		$additionalParameters = array('sort' => 'pageRankFile.txt desc');
  		/*if($flag == 1)
  			$results = $solr->search($correct_query, 0, $limit, $additionalParameters);
  		else*/
  		  	$results = $solr->search($query, 0, $limit, $additionalParameters);
  	}
  }
  catch (Exception $e)
  {
    // in production you'd probably log or email this error to an admin
    // and then show a special message to the user but for this example
    // we're going to show the full exception
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}

?>

<html>
  <head>
    <title>PHP Solr Client Example</title>
    <link href="http://code.jquery.com/ui/1.10.4/themes/ui-lightness/jquery-ui.css" rel="stylesheet"></link>
	<script src="http://code.jquery.com/jquery-1.10.2.js"></script>
	<script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
  </head>
  <body>
    <form  accept-charset="utf-8" method="get">
      <label for="q">Search : </label>
      <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
      <br/><br/>
      <input type="radio" name="algo" value="lucene"<?php if(isset($_REQUEST['algo']) && $_REQUEST['algo'] == 'lucene') {echo 'checked="checked"';} ?>> Lucene
      <input type="radio" name="algo" value="pagerank"<?php if(isset($_REQUEST['algo']) && $_REQUEST['algo'] == 'pagerank') {echo 'checked="checked"';} ?>> Page Rank
      <br/>
      <input type="submit"/>
    </form>
    <script>
	$(function() {
		var URL_PREFIX = "http://localhost:8983/solr/ircore/suggest?q=";
		var URL_SUFFIX = "&wt=json";
		var final_suggest = [];
		var previous= "";
		$("#q").autocomplete({
			source : function(request, response) {
				var q = $("#q").val().toLowerCase();
         		var sp =  q.lastIndexOf(' ');
         		if(q.length - 1 > sp && sp != -1)
         		{
          			final_query = q.substr(sp+1);
          			previous = q.substr(0,sp);
        		}
        		else
        		{
          			final_query = q.substr(0); 
        		}
				var URL = URL_PREFIX + final_query + URL_SUFFIX;
				$.ajax({
					url : URL,
					success : function(data) {
							  var docs = JSON.stringify(data.suggest.suggest);
							  var jsonData = JSON.parse(docs);
							  var result =jsonData[final_query].suggestions;
							  var j=0;
							  var suggest = [];
							  for(var i=0 ; i<5 && j<result.length ; i++,j++){
									if(final_query == result[j].term)
									{
								  		--i;
								  		continue;
									}
									for(var l=0;l<i && i>0;l++)
									{
									  	if(final_suggest[l].indexOf(result[j].term) >=0)
									  	{
											--i;
									  	}
									}
									if(suggest.length == 5)
									  break;
									if(suggest.indexOf(result[j].term) < 0)
									{
									  suggest.push(result[j].term);
									  if(previous == ""){
										final_suggest[i]=result[j].term;
									  }
									  else
									  {
										final_suggest[i] = previous + " ";
										final_suggest[i]+=result[j].term;
									  }
									}
							  }
							  response(final_suggest);
					},
					close: function () {
         				this.value='';
    					},
					dataType : 'jsonp',
 					jsonp : 'json.wrf'
 				});
 				},
 			minLength : 1
 			})
 		});
</script>
<?php

// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);

  if($flag == 1){
  	/*
	for($i = 0 ; $i < sizeof($query_terms); $i++)
  	{
  	$chk = SpellCorrector::correct($query_terms[$i]);
  	if($i == 0)
  		$correct_query = $correct_query . $chk;
  	else
  		$correct_query = $correct_query .' '. $chk;
  	}*/
	echo "Showing results for ", ucwords($query);
	$link = "http://10.0.2.15/solr-php-client/PageRank3.php?q=$correct_query";
	echo "<br>Search instead for <a href='$link'>$correct_query</a>";
}
?>
    <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
    <ol>
<?php
  // iterate result documents
  $csv = array_map('str_getcsv', file('UrlToHtml_Newday.csv'));
	
  foreach ($results->response->docs as $doc)
  {  
	$id = $doc->id;
  	$title = $doc->og_title;
  	$url = $doc->og_url;
  	$desc = $doc->og_description;
  	if($desc == "" || $desc == null)
  	{
  		$desc = "N/A";
	}
	if($title == "" || $title == null)
  	{
  		$title = "N/A";
	}
	if($url == "" || $url == null)
	{
	foreach($csv as $row)
		{
			$cmp = "/home/shriraj/Documents/IRAssign4/Newsday1/HTML_Files/" + $row[0];
			if($id == $cmp)
			{
				$url = $row[1];
				unset($row);
				break;
			}
		}
	}
	$snip = "";
	$query_terms = explode(" ", $query);
	$count = 0;
	$max = sizeof($query_terms);
	$prev_max = 0;
	$file_content = file_get_contents($id);
	$html = str_get_html($file_content);
	$content =  strtolower($html->plaintext);
	foreach(preg_split("/((\r?\n)|(\r\n?))/", $content) as $line)
	{
  		$sent = strtolower($line);
  		for($i = 0 ; $i < sizeof($query_terms); $i++)
  		{
  			$query_term_lower = strtolower($query_terms[i]);
  			if(strpos($sent, $query_term_lower) == 0)
  			{
  				$count = $count+1;
  			}
  		}
  		if($max==$count)
	    	{
	    		$snip = $sent;
	      		break;
	    	}
	    	else if($count > 0)
	    	{
	    	    $snip = $sent;
				break;
	    	}
	    	$count = 0;
    	
  	}
  	if($snip == "")
		$snip = $desc;
  	$pos_term = 0;
  	$start_pos = 0;
  	$end_pos = 0;
	for($i = 0 ; $i < sizeof($query_terms); $i++)
  	{
  	if (strpos(strtolower($snip), strtolower($query_terms[$i])) !== false) 
		{
		  $pos_term = strpos(strtolower($snip), strtolower($query_terms[$i]));
		  break;
		}
	}
	if($pos_term > 80)
	{
		$start_pos = $pos_term - 80; 
	}
	$end_pos = $start_pos + 160;
	if(strlen($snip) < $end_pos)
	{
		$end_pos = strlen($snip) - 1;
		$trim_end = "";
	}
	else
	{
		$trim_end = "....";
	}
	if(strlen($snip) > 160)
	{
		if($start > 0)
			$trim_beg = "....";
		else
			$trim_beg = "";
		$snip = $trim_beg.substr($snip , $start_pos , $end_pos - $start_pos + 1).$trim_end;
	}/*
	  $searchfor = $_GET["q"];
	  $ary = explode(" ",$searchfor);
	  $count = 0;
	  $max = 0;
	  $finalSnippet = "";
	  $HtmlText = substr($id,0,strlen($id)-5);

	  //$html_to_text_files_dir = "/Library/WebServer/Documents/solr-php-client/parsed/";
	  //$file_name = $html_to_text_files_dir . $HtmlText;

	  $html_to_text_files_dir = "/home/shriraj/Documents/IRAssign4/Newsday1/HTML_Files/";///////
	  $file_name = $html_to_text_files_dir . $id;

	  $file = fopen($file_name,"r");
	  while(! feof($file))
	  {
	    $snippet = fgets($file);
	    $elementlower = strtolower($snippet);
	    foreach($ary as $wd)
	    {
	      $wd = strtolower($wd);
	      if (strpos($elementlower, $wd) !== false) 
	      {
		$count = $count+1;
	      }
	    }
	    if($max<$count)
	    {
	      $finalSnippet = $snippet;
	      $max = $count;
	    }
	    else if($max==$count && $count>0)
	    {
	      if(strlen($finalSnippet)<strlen($snippet))
	      {
		$finalSnippet = $snippet;
		$max = $count;
	      }
	    }
	    $count = 0;
	  }
	  $pos = 0;
	  $wd = "";
	  foreach ($ary as $wd) {
	    if (strpos(strtolower($finalSnippet), strtolower($wd)) !== false) 
	    {
	      $pos = strpos(strtolower($finalSnippet), strtolower($wd));
	      break;
	    }
	  }
	  $start = 0;
	  if($pos>80)
	  {
	    $start = $pos - 80; 
	  }
	  else
	  {
	    $start = 0;
	  }
	  $end = $start + 160;
	  if(strlen($finalSnippet)<$end)
	  {
	    $end = strlen($finalSnippet)-1;
	    $post1 = "";
	  }
	  else
	  {
	    $post1 = "...";
	  }
	  
	  if(strlen($finalSnippet)>160)
	  {
	    if($start>0)
	      $pre = "...";
	    else
	      $pre = "";
	    
	    $finalSnippet = $pre . substr($finalSnippet,$start,$end-$start+1) . $post1;
	  }
	  if(strlen($finalSnippet)==0)
	  {
	    $finalSnippet = $desc;
	  }
	  fclose($file);
	  unset($row1);
	  error_reporting(E_ALL ^ E_NOTICE); */ 
  	echo "Title : <a href = '$url'>$title</a></br>
		URL : <a href = '$url'>$url</a></br>
		ID : $id</br>
		Snippet : ";
		$ary = explode(" ",$snip);
		$fullflag = 0;
		$snipper = "";
		
		foreach ($ary as $word)
		{
			$flag = 0;
			for($i = 0 ; $i < sizeof($query_terms); $i++)
			{
				if(stripos($word,$query_terms[$i])!=false)
				{

					$flag = 1;
					$fullflag = 1;
					break;
				}
			}
			if($flag == 1)
				$snipper =  $snipper.'<b>'.$word.'</b>';
			else
				$snipper =  $snipper.$word;	
			$snipper =  $snipper." ";	
		}
		#if($fullflag == 1)
		$words1 = preg_split('/\s+/', $query);
		foreach($words1 as $item)
			$snipper = str_ireplace($item, "<strong>".$item."</strong>",$snipper);
		echo $snipper."</br></br>";
		#else
			#echo "N/A</br></br>";
	}
}
?>
	</body>
</html>
