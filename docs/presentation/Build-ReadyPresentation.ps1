# Build the current French defense deck without closing presentations already open.
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$entries = Get-Content -Raw -Encoding UTF8 (Join-Path $PSScriptRoot 'slides-ready.json') | ConvertFrom-Json
$pptx = Join-Path $PSScriptRoot 'eventsProject-Tache-Soutenance.pptx'
$pdf = Join-Path $PSScriptRoot 'eventsProject-Tache-Soutenance.pdf'
$preview = Join-Path $PSScriptRoot 'preview-ready'
New-Item -ItemType Directory -Force $preview | Out-Null
function Color([string]$hex) {
    return [Convert]::ToInt32($hex.Substring(0,2),16) + 256*[Convert]::ToInt32($hex.Substring(2,2),16) + 65536*[Convert]::ToInt32($hex.Substring(4,2),16)
}
$bg = Color '0B1423'; $card = Color '17263B'; $ink = Color 'F6F8FC'
$muted = Color 'B7C8DC'; $accent = Color '55D9C1'; $amber = Color 'F4BF67'
function Box($slide,$x,$y,$w,$h,$fill) {
    $shape = $slide.Shapes.AddShape(5,$x,$y,$w,$h)
    $shape.Fill.ForeColor.RGB = $fill
    $shape.Line.Visible = 0
    return $shape
}
function Text($slide,[string]$value,$x,$y,$w,$h,$size,$color,[string]$name='Text',[bool]$bold=$false,[string]$font='Segoe UI',$minSize=0) {
    $shape = $slide.Shapes.AddTextbox(1,$x,$y,$w,$h)
    $shape.Name = "$name-$($slide.Shapes.Count)"
    $frame = $shape.TextFrame
    $frame.MarginLeft = 0; $frame.MarginRight = 0; $frame.MarginTop = 0; $frame.MarginBottom = 0
    $frame.WordWrap = -1; $frame.AutoSize = 0
    $frame.TextRange.Text = $value
    $frame.TextRange.Font.Name = $font
    $frame.TextRange.Font.Size = [single]$size
    $frame.TextRange.Font.Color.RGB = $color
    $frame.TextRange.Font.Bold = $(if ($bold) { -1 } else { 0 })
    $frame.TextRange.ParagraphFormat.SpaceAfter = 0
    $frame.TextRange.ParagraphFormat.SpaceBefore = 0
    $shape.Width = [single]$w
    $shape.Height = [single]$h
    if ($minSize -eq 0) { $minSize = $size }
    $fitSteps = 0
    while ($frame.TextRange.BoundHeight -gt ($h+1) -and $frame.TextRange.Font.Size -gt $minSize -and $fitSteps -lt 12) {
        $frame.TextRange.Font.Size = [single][Math]::Max([single]$minSize,[single]($frame.TextRange.Font.Size-1))
        $fitSteps++
    }
    return $shape
}
function Card($slide,$entry,$x,$y,$w,$h,$compact=$false) {
    [void](Box $slide $x $y $w $h $card)
    $titleSize = if ($compact) { 20 } else { 24 }
    $bodySize = if ($compact) { 14.5 } else { 18 }
    $headH = if ($compact) { 49 } else { 62 }
    if ($h -lt 100) { $bodySize = 13.5; $headH = 26 }
    [void](Text $slide $entry.title ($x+16) ($y+13) ($w-32) $headH $titleSize $accent 'CardTitle' $true 'Segoe UI' ($titleSize-2))
    [void](Text $slide $entry.body ($x+16) ($y+16+$headH) ($w-32) ($h-$headH-29) $bodySize $ink 'CardBody' $false 'Segoe UI' ($bodySize-1))
}
$app = New-Object -ComObject PowerPoint.Application
$previousAlerts = $app.DisplayAlerts
$app.DisplayAlerts = 1
$deck = $app.Presentations.Add(0)
$deck.PageSetup.SlideWidth = 960
$deck.PageSetup.SlideHeight = 540
try {
    foreach ($entry in $entries) {
        $slide = $deck.Slides.Add($deck.Slides.Count+1,12)
        $slide.FollowMasterBackground = 0
        $slide.Background.Fill.ForeColor.RGB = $bg
        [void](Text $slide "EVENTSPROJECT / $($entry.section)" 40 18 590 22 10.5 $muted 'Section' $true)
        $status = switch ($entry.status) {
            'historical' { 'DÉMONSTRATION DU 29/10/2026' }
            'pending' { 'REPRISE AWS À EFFECTUER' }
            default { 'VÉRIFIÉ AU 31/10/2026' }
        }
        $statusColor = if ($entry.status -eq 'verified') { $accent } else { $amber }
        [void](Text $slide $status 662 18 258 24 10 $statusColor 'Status' $true)
        if ($entry.layout -eq 'cover') {
            [void](Text $slide $entry.title 40 127 510 88 54 $ink 'Title' $true)
            [void](Text $slide $entry.description 43 225 470 112 26 $muted 'Description' $false 'Segoe UI' 23)
            for ($i=0;$i -lt 4;$i++) {
                $x = 568 + ($i%2)*181; $y = 146 + [Math]::Floor($i/2)*122
                [void](Box $slide $x $y 171 110 $card)
                [void](Text $slide $entry.cards[$i].title ($x+16) ($y+10) 145 52 35 $accent 'Metric' $true)
                [void](Text $slide $entry.cards[$i].body ($x+16) ($y+68) 145 33 14 $ink 'MetricLabel')
            }
            [void](Box $slide 40 416 880 54 $card)
            [void](Text $slide 'Release vérifiée • Pipeline unique vers EKS • Réactivation du Learner Lab requise' 56 430 850 31 17 $amber 'CoverState')
        } else {
            [void](Text $slide $entry.title 40 53 880 49 30 $ink 'Title' $true 'Segoe UI' 27)
            [void](Text $slide $entry.description 40 111 880 59 18 $muted 'Description' $false 'Segoe UI' 17)
            switch ($entry.layout) {
                'image' {
                    $path = Join-Path $root "docs/evidence/$($entry.screenshot)"
                    if (-not (Test-Path -LiteralPath $path)) { throw "Missing authentic evidence: $path" }
                    [void](Box $slide 350 186 570 286 (Color 'FFFFFF'))
                    $image = $slide.Shapes.AddPicture($path,0,-1,0,0,-1,-1)
                    $image.Name = "Evidence-$($deck.Slides.Count)"
                    $image.LockAspectRatio = -1
                    $image.Height = 270
                    if ($image.Width -gt 554) { $image.Width = 554 }
                    $image.Left = 350+(570-$image.Width)/2
                    $image.Top = 186+(286-$image.Height)/2
                    for ($i=0;$i -lt $entry.cards.Count;$i++) {
                        Card $slide $entry.cards[$i] 40 (186+$i*97) 292 91 $true
                    }
                }
                'code' {
                    [void](Box $slide 40 186 566 286 $card)
                    [void](Text $slide $entry.code 58 203 530 251 15.5 $ink 'Code' $false 'Consolas' 14)
                    for ($i=0;$i -lt $entry.cards.Count;$i++) {
                        Card $slide $entry.cards[$i] 624 (186+$i*150) 296 136 $true
                    }
                }
                default {
                    $count = $entry.cards.Count
                    $cols = if ($count -eq 4) { 2 } elseif ($count -le 3) { $count } else { 3 }
                    $rows = [Math]::Ceiling($count/$cols)
                    $w = (880-16*($cols-1))/$cols
                    $h = (286-16*($rows-1))/$rows
                    for ($i=0;$i -lt $count;$i++) {
                        $x = 40+($i%$cols)*($w+16)
                        $y = 186+[Math]::Floor($i/$cols)*($h+16)
                        Card $slide $entry.cards[$i] $x $y $w $h ($rows -gt 1)
                    }
                }
            }
        }
        [void](Text $slide $entry.evidence 40 492 850 30 8.5 $muted 'EvidenceSource')
        [void](Text $slide "$($deck.Slides.Count) / $($entries.Count)" 893 496 63 24 10 $muted 'PageNumber')
        foreach ($shape in $slide.NotesPage.Shapes) {
            if ($shape.HasTextFrame -eq -1) { $shape.TextFrame.TextRange.Text = '' }
        }
    }
    $deck.SaveAs($pptx,24)
    foreach ($slide in $deck.Slides) { $slide.Export((Join-Path $preview "Slide$($slide.SlideIndex).PNG"),'PNG',1600,900) }
    $deck.SaveAs($pdf,32)
    $overflows = @()
    foreach ($slide in $deck.Slides) {
        foreach ($shape in $slide.Shapes) {
            if ($shape.HasTextFrame -eq -1 -and $shape.TextFrame.HasText -eq -1) {
                if ($shape.TextFrame.TextRange.BoundHeight -gt ($shape.Height+2)) {
                    $overflows += @{slide=$slide.SlideIndex;shape=$shape.Name;height=$shape.Height;boundHeight=$shape.TextFrame.TextRange.BoundHeight}
                }
            }
        }
    }
    @{slides=$deck.Slides.Count;overflows=$overflows;pdf=(Test-Path $pdf);speakerNotes='empty'} |
        ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 (Join-Path $preview 'layout-check.json')
    Write-Output "Saved $($deck.Slides.Count) French slides, editable PPTX, PDF and previews. Text overflows: $($overflows.Count)."
} finally {
    $deck.Close()
    $app.DisplayAlerts = $previousAlerts
    [Runtime.InteropServices.Marshal]::ReleaseComObject($deck) | Out-Null
    [Runtime.InteropServices.Marshal]::ReleaseComObject($app) | Out-Null
}
