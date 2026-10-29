$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'docs/presentation'
$slides = Get-Content -Raw -Encoding UTF8 (Join-Path $output 'slides.json') | ConvertFrom-Json
$app = New-Object -ComObject PowerPoint.Application
$deck = $app.Presentations.Add(0)
$deck.PageSetup.SlideWidth = 960
$deck.PageSetup.SlideHeight = 540
function Add-Text($slide, $value, $x, $y, $w, $h, $size, $color) {
    $shape = $slide.Shapes.AddTextbox(1,$x,$y,$w,$h)
    $shape.TextFrame.TextRange.Text = $value
    $shape.TextFrame.TextRange.Font.Name = 'Segoe UI'
    $shape.TextFrame.TextRange.Font.Size = $size
    $shape.TextFrame.TextRange.Font.Color.RGB = $color
}
try {
    foreach ($entry in $slides) {
        $slide = $deck.Slides.Add($deck.Slides.Count+1,12)
        $slide.FollowMasterBackground = 0
        $slide.Background.Fill.ForeColor.RGB = 2762511
        Add-Text $slide 'EVENTSPROJECT / MODULE TACHE' 30 12 850 25 11 13953630
        Add-Text $slide $entry.title 30 42 900 52 28 16777215
        Add-Text $slide $entry.description 30 98 900 52 16 14803425
        if ($entry.screenshot) {
            $path = Join-Path $root "docs/evidence/$($entry.screenshot)"
            if (-not (Test-Path $path)) { throw "Missing screenshot: $path" }
            $image = $slide.Shapes.AddPicture($path,0,-1,0,156,-1,-1)
            $image.LockAspectRatio = -1
            $image.Height = 340
            if ($image.Width -gt 900) { $image.Width = 900 }
            $image.Left = (960-$image.Width)/2
            Add-Text $slide "Preuve : $($entry.screenshot)" 30 504 850 22 9 13953630
        } else {
            Add-Text $slide "eventsProject`nBranche Tache" 45 200 850 120 38 16777215
            Add-Text $slide 'Application, tests, preuves et automatisation' 45 365 850 80 24 13953630
        }
        Add-Text $slide "$($deck.Slides.Count)" 912 507 30 20 10 14803425
        # The user requested on-slide descriptions only: no speaker notes.
        $slide.NotesPage.Shapes.Placeholders.Item(2).TextFrame.TextRange.Text = ''
    }
    $deck.SaveAs((Join-Path $output 'eventsProject-Tache.pptx'),24)
    $preview = Join-Path $output 'preview-current'
    New-Item -ItemType Directory -Force $preview | Out-Null
    foreach ($renderSlide in $deck.Slides) { $renderSlide.Export((Join-Path $preview "Slide$($renderSlide.SlideIndex).PNG"),'PNG',1600,900) }
    Write-Output "Saved $($deck.Slides.Count) slides with descriptions and no speaker notes."
} finally {
    $deck.Close()
    [Runtime.InteropServices.Marshal]::ReleaseComObject($deck) | Out-Null
    [Runtime.InteropServices.Marshal]::ReleaseComObject($app) | Out-Null
}
