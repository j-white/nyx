\documentclass[12pt,letterpaper]{article}
\usepackage{amsmath,amsthm,amsfonts,amssymb,amscd}
\usepackage{enumerate}
\usepackage{fancyhdr}
\usepackage{mathrsfs}
\usepackage{graphicx}
\usepackage{longtable}
\usepackage[margin=2cm]{geometry}
\setlength{\parindent}{0.0in}
\setlength{\parskip}{0.05in}

\pagestyle{fancyplain}
\headheight 35pt
\lhead{\textbf{Nyx}}
\chead{\textbf{Image Encode/Decode Report}}
\rhead{}
\headsep 10pt

\setcounter{MaxMatrixCols}{32}

\begin{document}

\section{Source}

\begin{center}
\includegraphics[width=250px]{${helper.pathToTex(report.sourceFile.absolutePath)}}
\end{center}

Signal type: ${report.signalType}

Signal dimension: ${report.signalDimension}

\section{Encoding Summary}

Seconds to encode: ${report.secondsToEncode}s

Range dimension: ${report.rangeDimension}

Number of range partitions: ${report.numRangePartitions}

Domain dimension: ${report.domainDimension}

Number of domain partitions: ${report.numDomainPartitions}

Number of transforms: ${report.numTransforms}

<#list report.decodeReports as r>

\section{Decoding Summary ${r.scale}x}

\begin{center}
\includegraphics[width=250px]{${helper.pathToTex(r.destFile.absolutePath)}}
\end{center}

PSNR: ${r.PSNR} dB

Seconds to decode: ${r.secondsToDecode}

Decoded signal dimension: ${r.decodedSignal.dimension}

</#list>

\section{Fractal Details}

\subsection{Kernel parameters}

\begin{center}
    \begin{longtable}{| l | l | l | l |}
    \hline
    Range \# & Domain \# &  Distance & Kernel \\ \hline
<#list report.transforms as t>
    ${t.rangeBlockIndex} & ${t.domainBlockIndex} & ${t.distance} & \verb|${helper.mapToString(t.kernelParameters)}| \\ \hline
</#list>
    \end{longtable}
\end{center}

\subsection{Operators}

\emph{Decimation operator:}
\begin{align*}
 D = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.decimationOperator)}
 \end{bmatrix}
\end{align*}

\emph{Domain Fetch operator:}
\begin{align*}
 F_0 = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.getDomainFetchOperator(0))}
 \end{bmatrix}
\end{align*}

\begin{align*}
 F_1 = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.getDomainFetchOperator(1))}
 \end{bmatrix}
\end{align*}

\emph{Range Fetch operator:}
\begin{align*}
 R_0 = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.getRangeFetchOperator(0))}
 \end{bmatrix}
\end{align*}

\begin{align*}
 R_1 = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.getRangeFetchOperator(1))}
 \end{bmatrix}
\end{align*}

\emph{Put operator:}
\begin{align*}
 P_0 = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.getPutOperator(0))}
 \end{bmatrix}
\end{align*}

\begin{align*}
 P_1 = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.getPutOperator(1))}
 \end{bmatrix}
\end{align*}

\end{document}
