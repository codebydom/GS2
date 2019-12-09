package gpp.genesearch
import org.scalajs.dom
import dom.{document}
import org.scalajs.dom.ext.Ajax
import io.udash.wrappers.jquery._
import org.scalajs.dom.raw.{HTMLBodyElement}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.{ JSExportTopLevel}
import scala.util.{Failure, Success, Try}
import scala.scalajs.js.timers._
// NOTE NCBI API LIMITS 3 CALLS per SECOND so performance is purposfully hindered with timer to meet this NCBI requirement, has not been optimized
// Only manuel test cases have been performed on this software, next to be added is the automated test cases
// Main initated function
object search {
  def main(args: Array[String]): Unit = {
    println("Welcome to GS2, out with the old in with the new.") // Funny intro
    val parNode = document.createElement("div") // create main div for search
    parNode.setAttribute("class","input-group mb-3")
    parNode.setAttribute("style","padding:12px;box-shadow:1px 1px 3.5px grey;border-radius:20px;")
    val content = document.createElement("input") // create search input for the user
    content.setAttribute("id", "inputGroup-sizing-lg")
    content.setAttribute("oninput","input(this)")
    content.setAttribute("placeholder", "Enter gene symbol, Id, and/or transcript")
    val button = document.createElement("button") // Button for search input
    val groupAppend = document.createElement("div") // div for grouping button and content
    val bcont = document.createElement("div") // contains the button and content of input
    bcont.setAttribute("class","container")
    bcont.setAttribute("style","display:inline-block;vertical-align: middle;")
    val sbut = document.createElement("button") // Human and Mouse selector button and label
    val sbut2 = document.createElement("button")
    sbut.setAttribute("class","btn btn-outline-primary btn-lg active")
    sbut2.setAttribute("class","btn btn-outline-primary btn-lg")
    sbut.innerHTML = "Human"
    sbut2.innerHTML = "Mouse"
    sbut.setAttribute("id","hum")
    sbut2.setAttribute("id","mos")
    sbut.setAttribute("onClick","select(this)")
    sbut2.setAttribute("onClick","select(this)")
    sbut.setAttribute("style","outline:none;margin-top:6px;margin-left:2.5px;border-top-right-radius:0px;border-bottom-right-radius:0px;visibility: hidden;")
    sbut2.setAttribute("style","outline:none;margin-top:6px;border-left-radius:0px;border-top-left-radius:0px;border-bottom-left-radius:0px;visibility: hidden;")
    val selecText = document.createElement("label") // Label for the mouse and human button
    selecText.setAttribute("class","text-secondary")
    selecText.setAttribute("id","selectIt")
    selecText.setAttribute("style","margin-left:-11px;visibility:hidden;")
    selecText.innerHTML = "Select a species type"
    bcont.appendChild(selecText)
    bcont.appendChild(sbut)
    bcont.appendChild(sbut2) // adding button to button content container
    content.setAttribute("class","form-control")
    content.setAttribute("type","form")
    content.setAttribute("aria-describedby","basic-addon2")
    content.setAttribute("style","border-top-left-radius:10px;border-bottom-left-radius:10px;")
    groupAppend.setAttribute("class","input-group-append")
    button.setAttribute("onclick","addClickedMessage()")
    button.setAttribute("type","button")
    button.setAttribute("class","btn btn-primary")
    button.setAttribute("id","b1")
    button.setAttribute("style","border-top-right-radius:10px;border-bottom-right-radius:10px;")
    button.innerHTML = "Search <i class='material-icons' style='vertical-align:middle;margin-top:-5px'>search</i>"
    val genIntro = document.createElement("p") // Description of the App in the top of app
    genIntro.innerHTML = "<b><span class=\"text-primary\">GeneSearch shRNA (GS2)</span></b> is a search engine that returns shRNA and ORF downloadable CSV data from <a target='_blank' rel='noopener noreferrer' href='https://portals.broadinstitute.org/gpp/public/'>The Broad Institute</a> that relates to the genes searched. Enter in a Gene Symbol (BRCA1, EGFR), \nGene ID (672, 1956), Transcript ID (NM_007294.4, NM_001346897), or a comma separated list of all three (EGFR, 672, NM_001346897). \nGS2 validates every input utilizing the <a href='https://www.ncbi.nlm.nih.gov/' target='_blank' rel='noopener noreferrer'>NCBI</a> API to validate the gene's identity and species. <i><small>(Note: Too many API calls may cause unexpected errors. Check NCBI and Broad links in results below if CSV does not load)</small></i>"
    genIntro.setAttribute("class","text-secondary")
    genIntro.setAttribute("style","font-size:13px;padding-left:3px;padding-right:3px;margin-bottom:5px;")
    parNode.appendChild(genIntro)
    parNode.appendChild(content)
    groupAppend.appendChild(button)
    parNode.appendChild(groupAppend)
    val cntrCtr = document.createElement("div") // creating container to center other content divs
    cntrCtr.setAttribute("id","cc") // appending html elements together to
    cntrCtr.setAttribute("class","container") //
    parNode.appendChild(bcont)
    cntrCtr.appendChild(parNode)
    document.body.appendChild(cntrCtr)
  }
  // appendRes is the function that loads in the resources and html after NCBI apis have validated input and make call to this function
  def appendRes(specs: String, urlid: String, gene: String): Unit = {
      val dwnldbutton1 = document.createElement("button") // creating the three download buttons
      dwnldbutton1.setAttribute("style","margin-bottom:7px")
      val dwnldbutton2 = document.createElement("button")
      dwnldbutton2.setAttribute("style","margin-bottom:7px")
      val dwnldbutton3 = document.createElement("button")
      dwnldbutton3.setAttribute("style","margin-bottom:7px")
      val dwnldctnr = document.createElement("a")
      val dwnldctnr2 = document.createElement("a")
      val dwnldctnr3 = document.createElement("a")
      val row = document.createElement("div") // creating the row for the buttons
      val col1 = document.createElement("div") // creating the coloumns of the row for each button
      val col2 = document.createElement("div")
      val col3 = document.createElement("div")
      val gtitle = document.createElement("h5")
      if (gene!="none" && gene!="trans") { // If there is a gene name and no transcript, use this output for file download link, when a gene symbol has to be validated
        gtitle.innerHTML =   "<span class='badge badge-primary'>"+gene+"</span> <a target='_blank' rel='noopener noreferrer' href='https://portals.broadinstitute.org/gpp/public/gene/details?geneId="+urlid+"'><span >Gene Id: "+urlid+"</span></a>"+" <a target='_blank' rel='noopener noreferrer' href='https://www.ncbi.nlm.nih.gov/gene/"+urlid+"'<span class='badge badge-light border'>NCBI</span></a> "+"<small><i>(" + specs + ")</i><small>"
        dwnldbutton1.innerHTML = "<b>100%</b> matching shRNA constructs for "+gene+" CSV <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px' >cloud_download</i>"
        dwnldbutton2.innerHTML = "<b>>84%</b> matching shRNA constructs for "+gene+" CSV <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px'>cloud_download</i>"
        dwnldbutton3.innerHTML = "<b>ORF</b> constructs that match to "+gene+" CSV <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px'>cloud_download</i>"
        dwnldctnr.setAttribute("href","https://portals.broadinstitute.org/gpp/public/gene/details?view=csv&grid=1&grid=1&geneId="+urlid)
        dwnldctnr.setAttribute("download","shRNA-geneId-"+urlid+"-100.csv")
        dwnldctnr2.setAttribute("href","https://portals.broadinstitute.org/gpp/public/gene/details?view=csv&grid=2&grid=2&geneId="+urlid)
        dwnldctnr2.setAttribute("download","shRNA-geneId-"+urlid+"-84.csv")
        dwnldctnr3.setAttribute("href","https://portals.broadinstitute.org/gpp/public/gene/details?view=csv&grid=6&grid=6&geneId="+urlid)
        dwnldctnr3.setAttribute("download","ORF-geneId-"+urlid+"-ORF.csv")
      } else if(gene=="none"){ // this is for when only a gene id is validated by the api calls
        gtitle.innerHTML = " <a target='_blank' rel='noopener noreferrer' href='https://portals.broadinstitute.org/gpp/public/gene/details?geneId="+urlid+"'><span >Gene Id: "+urlid+"</span></a>"+" <a target='_blank' rel='noopener noreferrer' href='https://www.ncbi.nlm.nih.gov/gene/"+urlid+"'<span class='badge badge-light border'>NCBI</span></a> "+"<small><i>(" + specs + ")</i><small>"
        dwnldbutton1.innerHTML = "<b>100%</b> matching shRNA constructs for Gene Id: "+urlid.toString+" CSV <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px'>cloud_download</i>"
        dwnldbutton2.innerHTML = "<b>>84%</b> matching shRNA constructs for Gene Id: "+urlid.toString+" CSV <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px'>cloud_download</i>"
        dwnldbutton3.innerHTML = "<b>ORF</b> constructs matching to Gene Id: "+urlid.toString+" CSV <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px'>cloud_download</i>"
        dwnldctnr3.setAttribute("href","https://portals.broadinstitute.org/gpp/public/gene/details?view=csv&grid=6&grid=6&geneId="+urlid)
        dwnldctnr3.setAttribute("download","ORF-geneId-"+urlid+"-ORF.csv")
        dwnldctnr.setAttribute("href","https://portals.broadinstitute.org/gpp/public/gene/details?view=csv&grid=1&grid=1&geneId="+urlid)
        dwnldctnr.setAttribute("download","shRNA-geneId-"+urlid+"-100.csv")
        dwnldctnr2.setAttribute("href","https://portals.broadinstitute.org/gpp/public/gene/details?view=csv&grid=2&grid=2&geneId="+urlid)
        dwnldctnr2.setAttribute("download","shRNA-geneId-"+urlid+"-84.csv")
      } else if (gene=="trans"){ // this is for when only a transcript is validated by api calls
        gtitle.innerHTML = "<a target='_blank' rel='noopener noreferrer' href='https://portals.broadinstitute.org/gpp/public/trans/details?transName="+urlid+"'><span >Transcript Id: "+urlid+"</span></a>"+" <a target='_blank' rel='noopener noreferrer' href='https://www.ncbi.nlm.nih.gov/nuccore/"+urlid+"'<span class='badge badge-light border'>NCBI</span></a> "+"<small><i>(" + specs + ")</i><small>"
        dwnldbutton1.innerHTML = "<b>100%</b> matching shRNA constructs for Transcript: "+urlid.toString+" CSV  <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px'>cloud_download</i>"
        dwnldbutton2.innerHTML = "<b>>84%</b> matching shRNA constructs for  Transcript: "+urlid.toString+" CSV  <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px'>cloud_download</i>"
        dwnldbutton3.innerHTML = "<b>ORF</b> constructs matching to Transcript: "+urlid.toString.toUpperCase()+" CSV <i class=\"material-icons\" style='vertical-align:middle;margin-top:-5px'>cloud_download</i>"
        dwnldctnr3.setAttribute("href","https://portals.broadinstitute.org/gpp/public/trans/details?view=csv&grid=3&transName="+urlid.toUpperCase())
        dwnldctnr3.setAttribute("download","ORF-WT-Transcript-"+urlid+"-ORF.csv")
        dwnldctnr.setAttribute("href","https://portals.broadinstitute.org/gpp/public/trans/details?view=csv&grid=1&transName="+urlid.toUpperCase())
        dwnldctnr.setAttribute("download","shRNA-WT-Transcript-"+urlid+"-100.csv")
        dwnldctnr2.setAttribute("href","https://portals.broadinstitute.org/gpp/public/trans/details?view=csv&grid=2&transName="+urlid.toUpperCase())
        dwnldctnr2.setAttribute("download","shRNA-WT-Transcript-"+urlid+"-84.csv")
      }
      document.getElementById("cc2").appendChild(gtitle)
      row.setAttribute("class","row") // adding elements necesary for format and appending html to form rows for buttons
      col1.setAttribute("class","col-sm")
      col2.setAttribute("class","col-sm")
      col3.setAttribute("class","col-sm")
      dwnldbutton1.setAttribute("class","btn btn-success")
      dwnldctnr.appendChild(dwnldbutton1)
      col1.appendChild(dwnldctnr)
      dwnldbutton2.setAttribute("class","btn btn-info")
      dwnldctnr2.appendChild(dwnldbutton2)
      col2.appendChild(dwnldctnr2)
      dwnldbutton3.setAttribute("class","btn btn-secondary")
      dwnldctnr3.appendChild(dwnldbutton3)
      col3.appendChild(dwnldctnr3)
      row.appendChild(col1)
      row.appendChild(col2)
      row.appendChild(col3) // Finally loading in all the data and appending the row
      document.getElementById("cc2").appendChild(row)
      jQ("#spiny").css("display","none")// getting rid of spinny and making sure the result explanations (res1,res2,#r1,#r2)
      jQ("#spiny2").css("display","none")
      jQ("#r1").css("visibility","visible")
      jQ("#r2").css("visibility","visible")
      jQ("#r3").css("visibility","visible")
  }
  // Function for when the user submits a search
  @JSExportTopLevel("addClickedMessage")
  def addClickedMessage(): Unit = {
    jQ("#cc2").remove() // clear content div
    val cntrCtr2 = document.createElement("div") // creating container div
    cntrCtr2.setAttribute("id","cc2")
    cntrCtr2.setAttribute("class","container")
    // Clearing out descriptions
    // removing result explanations for new calls
    jQ("#r1").remove()
    jQ("#r2").remove()
    jQ("#r3").remove()
    document.body.appendChild(cntrCtr2)
    // RESULT DESCRIPTIONS RENDERING (COLORFUL DESCRIPTIONS ON THE BOTTOM OF APP)
    val res1 = document.createElement("div")
    val res2 = document.createElement("div")
    val res3 = document.createElement("div")
    res1.setAttribute("id","r1")
    res2.setAttribute("id","r2")
    res3.setAttribute("id","r3")
    res1.setAttribute("class","border border-success container")
    res2.setAttribute("class","border border-info container")
    res3.setAttribute("class","border border-secondary container")
    res1.innerHTML = "<p style='margin-bottom:0px'><b class='text-success' style='font-size:14px'><span class='badge badge-success border' style='font-size:12px'>100% </span> of shRNA constructs match to this Gene Id/transcript Specificity-Defining Region (SDR)</b></p><p style='margin-bottom:0px'>This list includes all shRNAs that have a perfect SDR match to the gene Id/transcript above download buttons, regardless of what transcript they were originally designed to target. For example, this list can include shRNAs that were originally designed to target: (i) a different isoform or obsolete version of this transcript (as annotated by NCBI), (ii) a transcript of an orthologous gene (in this collection, generally human-to-mouse or mouse-to-human), or (iii) a transcript of a different gene (from the same or different taxon).</p>"
    res2.innerHTML = "<p style='margin-bottom:0px'><b class='text-info' style='font-size:14px'><span class='badge badge-info border' style='font-size:12px'> >84% </span> of shRNA constructs match to this Gene Id/transcript</b></p><p style='margin-bottom:0px'>This list includes shRNAs that have at least a >84% (16 of 19 bases) Specificity-Defining Region match to the gene Id/transcript above the download buttons, regardless of what gene id/transcript they were originally designed to target. For example, this list can include shRNAs that were originally designed to target: (i) a different isoform or obsolete version of this transcript (as annotated by NCBI), (ii) a transcript of an orthologous gene (in this collection, generally human-to-mouse or mouse-to-human), or (iii) a transcript of a different gene (from the same or different taxon). NOTE: this download is a superset of the result set including 100% matches.</p>"
    res3.innerHTML = "<p style='margin-bottom:0px'><b class='text-secondary' style='font-size:14px'><span class='badge badge-secondary border' style='font-size:12px'>ORF </span> constructs matching to Gene Id/transcript </b></p><p style='margin-bottom:0px'>This list includes ORFs that match with the gene Id/transcript above the download buttons, ORFs can regulate eukaryotic gene expression through the sythesis or transportation of amino acids </p>"
    res1.setAttribute("style","padding: 6px;box-shadow:1px 1px 3.5px grey;border-radius:20px;font-size:12px;margin-top:26.9px;margin-bottom:10px;width:80%;border-width:2px!important;visibility: hidden;")
    res2.setAttribute("style","padding: 6px;box-shadow:1px 1px 3.5px grey;border-radius:20px;font-size:12px;margin-bottom:10px;width:80%;border-width:2px!important;visibility: hidden;")
    res3.setAttribute("style","padding: 6px;box-shadow:1px 1px 3.5px grey;border-radius:20px;font-size:12px;margin-bottom:10px;width:80%;border-width:2px!important;visibility: hidden;")
    jQ("#spiny").remove() // making sure there is no spinning wheel still occuring
    val sp = document.createElement("div") // creating SPINNER FOR LOADING
    sp.setAttribute("class","d-flex justify-content-center")
    sp.setAttribute("id","spiny")
    val sp2 = document.createElement("div")
    sp2.setAttribute("class","spinner-border text-primary")
    sp2.setAttribute("id","spiny2")
    sp2.setAttribute("role","status")
    val spin = document.createElement("span")
    spin.innerHTML = "Loading..."
    spin.setAttribute("class","sr-only")
    sp2.appendChild(spin)
    sp.appendChild(sp2)
    document.body.appendChild(sp) // adding showing spinner
    document.body.appendChild(res1) // adding hidden responses to be revealed once data is loaded in
    document.body.appendChild(res2)
    document.body.appendChild(res3)
    val inputtext = jQ("#inputGroup-sizing-lg").value() // getting value of search input
    if (inputtext.toString==""){
      dom.window.alert("Enter in a gene symbol, gene Id, or transcript Id in the search box") // warning about not entering anything into search box
      jQ("#spiny").css("display","none") // getting rid of spinner
      jQ("#spiny2").css("display","none")
      return
    }
    val lurl = inputtext.toString().split(",") // creating list from input
    val thelist = lurl.distinct // list to make sure no repeats are entered to make sure duplicates do not cause api call overload
    for (g <- thelist) { // for loop to loop through input list of unique values
      val w = g.toString.replaceAll("\\s", "")// value used to retrieve data
      var timer = 0 // timer to delay send outs because of NCBI policy
      if (thelist.length==2){
        timer = 2800
      } else if (thelist.length==3){
        timer = 3600
      }else if (thelist.length>=4){
        timer = 4000
      }
      if (g==lurl(0) && thelist.length<3){
        timer = 0
      }
      setTimeout(timer) {
        var url = ""
        if (Try(w.toDouble).isSuccess) { // if the input is a number, validate that it is a gene id
          val ajxNum = Ajax.get("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=gene&id=" + w)// Validating Species of gene id
          ajxNum.onComplete {
            case Success(xhr) =>
              if (xhr.responseText.contains("taxname")) {
                val taxo = xhr.responseText.split("taxname ")(1).split(",")(0).replaceAll("\"", "") // TODO getting taxonomy (again can be seperate function!! **NOTE** )
                if (taxo.toLowerCase() == "homo sapiens" || taxo.toLowerCase() == "mus musculus") {
                  appendRes(taxo, w, "none")
                } else {
                  error(w)
                }
              } else {
                error(w)
              }
            case Failure(e) =>
              error(w)
          }
        } else if (w.contains("_")) { // if the input has a _ it is treated as a transcript
          if (w.contains(".")) { // this means this transcript has version and should search for it
            //check to see transcript is a mouse or a human, if not an error is returned
            val ajxnMaus = Ajax.get("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&id=" + w)
            ajxnMaus.onComplete {
              case Success(xhr) =>
                val taxon = xhr.responseText.split("taxname ")(1).split(",")(0).replaceAll("\"", "") // parsing the response text for taxology
                println(taxon)
                if (taxon.toLowerCase() == "homo sapiens" || taxon.toLowerCase() == "mus musculus") { // transcript is identified as human/mouse and has valid ncbi ids, call to dwnld button creating fuctions
                  appendRes(taxon, w, "trans")
                } else {
                  error(w)
                  return
                }
              case Failure(e) =>
                error(w)
                return
            }
          } else { // if there is no transcript version associated with the input
            val ajxNM = Ajax.get("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&id=" + w)
            ajxNM.onComplete {
              case Success(xhr) =>
                var version = ""
                val tax = xhr.responseText.split("taxname ")(1).split(",")(0).replaceAll("\"", "") // parse out taxology
                if (!w.contains("NR")) {
                  version = xhr.responseText.split(w)(1).split("}")(0).split("version")(1).replaceAll("\\s", "") // this is parsing for NR_ transcript for version
                } else {
                  if (xhr.responseText.split("version").length > 0) {
                    version = xhr.responseText.split("version")(1).split("}")(0).replaceAll("\\s", "") // returning nm version
                  }
                  else {
                    error(w)
                    return
                  }
                }
                // here we also check to see if taxonomy is human or mouse, this can be made into function instead of repeating code, do this then seperate out based on version number
                if (tax.toLowerCase() == "homo sapiens" || tax.toLowerCase() == "mus musculus") { // assuming there are no nc transcripts, should there be?
                  if (!w.contains("NC")) {
                    appendRes(tax, w + "." + version, "trans")
                  } else {
                    error(w)
                    return
                  }
                } else {
                  error(w)
                  return
                }
              case Failure(e) =>
                error(w)
                return
            }
          }
        } else { // if the input was not a transcript or a gene id, we default to attempting to finding input as a gene symbol
          if (document.getElementById("hum").classList.contains("active")) { // based on value selected for the species input, based on that either using mouse or human url to validate
            url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=(" + w + "[gene])%20AND%20(Homo%20sapiens[orgn])%20AND%20alive[prop]%20NOT%20newentry[gene]&sort=weight"
          } else if (document.getElementById("mos").classList.contains("active")) {
            url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=(" + w + "[gene])%20AND%20(Mus%20musculus[orgn])%20AND%20alive[prop]%20NOT%20newentry[gene]&sort=weight"
            val spc = "Mus musculus"
          } else {
            // if nothing is selected which is basically impossible unless u edit the html
            dom.window.alert("Please select either Human or Mouse")
            jQ("#spiny").css("display","none")
            jQ("#spiny2").css("display","none")
            return
          }
        }
        if (url != "") { // check to make sure there is a url at this point, uncessary at this point, may be able to remove
          val ajx = Ajax.get(url) // make the ajax call based on human or mouse
          ajx.onComplete {
            case Success(xhr) =>
              if (xhr.responseXML.getElementsByTagName("Id").length != 0) { // if there are id associated at the link mean that gene symbol is validated
                val ilist = xhr.responseXML.getElementsByTagName("Id").length // get list of all the ids for vague gene symbols such as "CD"
                var a = 0
                for (a <- 0 until ilist) { // loop through list of the ids in the response
                  val geneIn = xhr.responseXML.getElementsByTagName("Id")(a).textContent // getting the id from the parse
                  // setting timmer to space out calls here, is necessary for vague gene symbols with too many
                  var t2 = 0
                  if (g==lurl(0) && thelist.length<2){
                    t2 = 0
                  } else {
                    t2 = 2000
                  }
                  setTimeout(t2) {
                    val ajxNum2 = Ajax.get("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=gene&id=" + geneIn) // creating ajax call for gene id
                    ajxNum2.onComplete {
                      case Success(xhr) =>
                        if (xhr.responseText.contains("taxname")) { // if there is a taxonomy associated
                          val taxo = xhr.responseText.split("taxname ")(1).split(",")(0).replaceAll("\"", "") // gets taxonomy
                          val gene = xhr.responseText.split("gene " + "{")(1).split(",")(0).replaceAll("locus ", "").replaceAll("\"", "").replaceAll("\\s", "") // gets specific gene for cases like "CD"
                          println(gene);
                          if (taxo.toLowerCase() == "homo sapiens" || taxo.toLowerCase() == "mus musculus") {
                            appendRes(taxo, geneIn, gene) // on match make a call to the appending resources/response function
                          } else {
                            error(geneIn) // rest of lines are error statements which occur if any of the validating if statements fail, they give same general output message with input which the app failed to retrieve
                            return
                          }
                        } else {
                          error(geneIn)
                          return
                        }
                      case Failure(e) =>
                        error(geneIn)
                        return
                    }
                  }
                }
              } else {
                error(w)
                return
              }
            case Failure(e) =>
              error(w)
              return
          }
        }
      }
    }
  }
  // making sure that only one button for the Human and mouse can be selected at a time, would be better with a toggle function! could improve.
  @JSExportTopLevel("select")
  def select(i: HTMLBodyElement): Unit = {
    if (i.innerHTML=="Human"){
      document.getElementById("mos").setAttribute("class","btn btn-outline-primary btn-lg")
      i.setAttribute("class","btn btn-outline-primary active btn-lg")
    } else {
      i.setAttribute("class","btn btn-outline-primary active btn-lg")
      document.getElementById("hum").setAttribute("class","btn btn-outline-primary btn-lg")
    }
  }
  //Function for showing and hidding the species select button only when its necessary, species selection only effects gene symbols
  @JSExportTopLevel("input")
  def input(i: HTMLBodyElement): Unit = {
    val inputtext2 = jQ("#"+i.getAttribute("id")).value()
    if(inputtext2.toString.length>0){
      jQ("#mos").css("visibility","visible");
      jQ("#hum").css("visibility","visible")
      jQ("#selectIt").css("visibility","visible")
    } else {
      jQ("#mos").css("visibility","hidden")
      jQ("#hum").css("visibility","hidden")
      jQ("#selectIt").css("visibility","hidden")
    }
  }
  // general error display for user
  def error(i:String): Unit = {
    jQ("#spiny").css("display","none")
    jQ("#spiny2").css("display","none")
    dom.window.alert("Could not find gene/transcript: "+i+"\n\nMake sure the species and search inputs are correct."+"\n\nToo many NCBI API Calls too quickly can cause unexpected errors.")
  }
}