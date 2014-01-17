\documentclass[12pt,letterpaper]{article}
\usepackage{amsmath,amsthm,amsfonts,amssymb,amscd}
%\usepackage{fullpage}
%\usepackage{lastpage}
\usepackage{enumerate}
\usepackage{fancyhdr}
\usepackage{mathrsfs}
\usepackage{graphicx}
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
\includegraphics[width=250px]{${report.sourceFile.name}}
\end{center}

Signal type: ${report.signalType}

Signal dimension: ${report.signalDimension}

\section{Encoding Summary}

Seconds to encode: ${report.secondsToEncode}s

Size of signal in bytes: ${report.sizeOfSignalInBytes}

Size of fractal in bytes: ${report.sizeOfFractalInBytes}

Range dimension: ${report.rangeDimension}

Number of range partitions: ${report.numRangePartitions}

Domain dimension: ${report.domainDimension}

Number of domain partitions: ${report.numDomainPartitions}

Number of transforms: ${report.numTransforms}

<#list report.decodeReports as r>

\section{Decoding Summary ${r.scale}x}

\includegraphics[width=250px]{${r.destFile.name}}

Seconds to decode: ${r.secondsToDecode}

Decoded signal dimension: ${r.decodedSignal.dimension}

Decoded size in bytes: ${r.decodedSignal.sizeInBytes}

</#list>

\section{Fractal Details}

\subsection{Operators}

\emph{Decimation operator:}
\begin{align*}
 D = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.decimationOperator)}
 \end{bmatrix}
\end{align*}

\emph{Fetch operator:}
\begin{align*}
 F_I = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.getRangeFetchOperator(0))}
 \end{bmatrix}
\end{align*}

\emph{Put operator:}
\begin{align*}
 P_I = 
 \begin{bmatrix}
  ${helper.matrixToTex(report.getPutOperator(0))}
 \end{bmatrix}
\end{align*}

\subsection{Kernel parameters}

** Scatter plot of x=range\#, y=distance **

\begin{center}
    \begin{tabular}{| l | l | l | l |}
    \hline
    Range \# & Domain \# &  Distance & Kernel \\ \hline
<#list report.transforms as t>
    ${t.rangeBlockIndex} & ${t.domainBlockIndex} & ${t.distance} & \verb|${helper.mapToString(t.kernelParameters)}| \\ \hline
</#list>
    \end{tabular}
\end{center}

\end{document}
